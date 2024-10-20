package com.exe201.ilink.service.Impl;

import com.exe201.ilink.Util.CustomerOrderSpecification;
import com.exe201.ilink.model.entity.*;
import com.exe201.ilink.model.enums.*;
import com.exe201.ilink.model.exception.ILinkException;
import com.exe201.ilink.model.payload.dto.OrderProductDTO;
import com.exe201.ilink.model.payload.request.OrderInfo;
import com.exe201.ilink.model.payload.response.OrderHistoryElement;
import com.exe201.ilink.model.payload.response.OrderHistoryResponse;
import com.exe201.ilink.model.payload.response.PaymentStatementResponse;
import com.exe201.ilink.repository.*;
import com.exe201.ilink.service.CustomerOrderService;
import com.exe201.ilink.service.EmailService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.config.Configuration;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CustomerOrderServiceImpl implements CustomerOrderService {

    private final AccountRepository accountRepository;
    private final CustomerOrderRepository customerOrderRepository;
    private final ModelMapper modelMapper;
    private final ProductRepository productRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final TransactionRepository transactionRepository;
    private final EmailService emailService;
    private final ShopRepository shopRepository;

    @Override
    public void saveOrder(OrderInfo orderInfo, String orderCode) {
        Account account = accountRepository.findById(orderInfo.getAccountId())
            .orElseThrow(() -> new ILinkException(HttpStatus.INTERNAL_SERVER_ERROR, "Request fails. Account not found"));

        if (!RoleName.contains(account.getRole().getRoleName())) {
            throw new ILinkException(HttpStatus.INTERNAL_SERVER_ERROR, "Request fails. Role is not valid");
        }

        modelMapper.getConfiguration().setFieldMatchingEnabled(true)
            .setFieldAccessLevel(Configuration.AccessLevel.PRIVATE)
            .setAmbiguityIgnored(true)
            .setSkipNullEnabled(false)
            .setMatchingStrategy(MatchingStrategies.STRICT);

        CustomerOrder order = modelMapper.map(orderInfo, CustomerOrder.class);

        Transaction transaction = Transaction.builder()
            .paymentMethod("Online payment")
            .build();

        transactionRepository.save(transaction);

        order.setCode(orderCode);
        order.setAccount(account);
        order.setTransaction(transaction);
        order.setStatus(PaymentStatus.PENDING.name());
        customerOrderRepository.save(order);

        //Update Product Stock
        orderInfo.getProducts().forEach(orderProductDTO -> {
            Product product = productRepository.findById(orderProductDTO.getProductId())
                .orElseThrow(() -> new ILinkException(HttpStatus.INTERNAL_SERVER_ERROR, "Request fails. Product not found"));

            if (!product.getStatus().equals(ProductStatus.ACTIVE.getStatus())) {
                throw new ILinkException(HttpStatus.INTERNAL_SERVER_ERROR, "Request fails. Product is not active");
            }

            if ((product.getStock() - orderProductDTO.getQuantity()) < 0) {
                throw new ILinkException(HttpStatus.INTERNAL_SERVER_ERROR, "Request fails. Product stock is not enough");
            }


        });

        List<OrderDetail> orderDetails = new ArrayList<>();
        orderInfo.getProducts().forEach(product -> {
            OrderDetail orderDetail = OrderDetail.builder()
                .quantity(product.getQuantity())
                .price(product.getUnitPrice())
                .lineTotal(product.getLineTotal())
                .product(productRepository.findById(product.getProductId())
                    .orElseThrow(() -> new ILinkException(HttpStatus.INTERNAL_SERVER_ERROR, "Request fails. Product not found")))
                .order(order)
                .build();
            orderDetails.add(orderDetail);
        });

        orderDetailRepository.saveAll(orderDetails);
    }

    @Override
    @Transactional
    public PaymentStatementResponse updateOrder(String paymentId, String paymentCode, String paymentStatus, String orderCode, boolean cancel) {

        CustomerOrder customerOrder = customerOrderRepository.findByCode(orderCode)
            .orElseThrow(() -> new ILinkException(HttpStatus.INTERNAL_SERVER_ERROR, "Request fails. Order not found"));

        Account buyerAccount = accountRepository.findById(customerOrder.getAccount().getAccountId())
            .orElseThrow(() -> new ILinkException(HttpStatus.INTERNAL_SERVER_ERROR, "Request fails. Account not found"));

        //Update Order Status
        customerOrder.setStatus(paymentStatus);
        customerOrderRepository.save(customerOrder);

        //Update Transaction
        Transaction transaction = transactionRepository.findById(customerOrder.getTransaction().getId())
            .orElseThrow(() -> new ILinkException(HttpStatus.INTERNAL_SERVER_ERROR, "Request fails. Transaction not found"));

        transaction.setPaymentCode(paymentCode);
        transaction.setPaymentId(paymentId);
        transaction.setOrder(customerOrder);
        transactionRepository.save(transaction);

        //Send email to Buyer and Seller
        if (!paymentStatus.equals("CANCELLED") && paymentCode.equals("00") && !cancel) {

            List<OrderDetail> details = orderDetailRepository.findByOrderId(customerOrder.getId())
                .orElseThrow(() -> new ILinkException(HttpStatus.INTERNAL_SERVER_ERROR, "Request fails. Order detail not found"));
            //Send email to Seller
            List<Shop> shops = details.stream().map(orderDetail -> orderDetail.getProduct().getShop()).toList();

            //send email to each shop in Bill
            shops.forEach(shop -> {

                Account sellerAccount = accountRepository.findById(shop.getAccount().getAccountId())
                    .orElseThrow(() -> new ILinkException(HttpStatus.INTERNAL_SERVER_ERROR, "Request fails. Account not found"));

                //Filter OrderDetail by Shop
                List<OrderDetail> orderDetails = new ArrayList<>();
                details.forEach(orderDetail -> {
                    if (orderDetail.getProduct().getShop().getShopId().equals(shop.getShopId())) {
                        orderDetails.add(orderDetail);
                    }
                });

                //Map OrderDetail to OrderProductDTO
                List<OrderProductDTO> orderProductDTOS = orderDetails.stream().map(orderDetail -> OrderProductDTO.builder()
                    .productId(orderDetail.getProduct().getId())
                    .productName(orderDetail.getProduct().getProductName())
                    .quantity(orderDetail.getQuantity())
                    .unitPrice(orderDetail.getPrice())
                    .lineTotal(orderDetail.getLineTotal())
                    .build()).toList();

                //Update Product Stock
                orderProductDTOS.forEach(orderProductDTO -> {
                    Product product = productRepository.findById(orderProductDTO.getProductId())
                        .orElseThrow(() -> new ILinkException(HttpStatus.INTERNAL_SERVER_ERROR, "Request fails. Product not found"));
                    product.setStock(product.getStock() - orderProductDTO.getQuantity());

                    if (product.getStock() < 0) {
                        throw new ILinkException(HttpStatus.INTERNAL_SERVER_ERROR, "Request fails. Product stock is not enough");
                    }

                    if (product.getStock() == 0) {
                        product.setStatus(ProductStatus.CLOSED.name());
                    }
                    productRepository.save(product);
                });

                // Định dạng DateTimeFormatter để parse chuỗi thành LocalDateTime
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

                // Parse chuỗi thành LocalDateTime
                LocalDateTime dateTime = LocalDateTime.parse(customerOrder.getCreatedDate().toString(), formatter);

                // Tách ngày và giờ
                LocalDate date = dateTime.toLocalDate();  // Lấy phần ngày
                LocalTime time = dateTime.toLocalTime();  // Lấy phần giờ


                try {
                    //Send email to Seller
                    emailService.sendMimeMessageForSeller(
                        sellerAccount.fullName(),
                        buyerAccount.fullName(),
                        date.toString(),
                        time.toString(),
                        sellerAccount.getEmail(),
                        customerOrder.getCode(),
                        orderProductDTOS,
                        customerOrder.getTotalPrice(),
                        customerOrder.getCustomerName(),
                        customerOrder.getPhone(),
                        customerOrder.getAddress(),
                        EmailTemplateName.NEW_ORDER_INFORM.getName(),
                        "[Souvi] You have an order !");
                } catch (MessagingException e) {
                    throw new RuntimeException(e);
                }

                try {
                    //Send email to Buyer
                    emailService.sendMimeMessageForSeller(
                        sellerAccount.fullName(),
                        buyerAccount.fullName(),
                        date.toString(),
                        time.toString(),
                        sellerAccount.getEmail(),
                        customerOrder.getCode(),
                        orderProductDTOS,
                        customerOrder.getTotalPrice(),
                        customerOrder.getCustomerName(),
                        customerOrder.getPhone(),
                        customerOrder.getAddress(),
                        EmailTemplateName.ORDER_INFORM.getName(),
                        "[Souvi] Your order details information !");
                } catch (MessagingException e) {
                    throw new RuntimeException(e);
                }
            });

        }

        return PaymentStatementResponse.builder()
            .paymentId(paymentId)
            .paymentCode(paymentCode)
            .paymentStatus(paymentStatus)
            .orderCode(orderCode)
            .cancel(cancel)
            .build();
    }

    @Override
    public OrderHistoryResponse getOrderDetailsForAdmin(int pageNo, int pageSize, ProductSort sortBy, String status, UUID sellerId, UUID buyerId, Date startDate, Date endDate) {
        Sort sort = Sort.by(sortBy.getDirection(), sortBy.getField());
        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);

        Specification<CustomerOrder> spec = Specification.where(
                CustomerOrderSpecification.hasStatus(status))
            .and(CustomerOrderSpecification.hasBuyerId(buyerId))
            .and((CustomerOrderSpecification.hasSellerId(sellerId))
                .and((CustomerOrderSpecification.isCreatedBetween(startDate, endDate)))
            );

        return getOrderHistoryResponse(spec, pageable);
    }


    @Override
    public OrderHistoryResponse getOrderDetailsForBuyer(int pageNo, int pageSize, ProductSort sortBy, String status, UUID buyerId, Date startDate, Date endDate) {
        Sort sort = Sort.by(sortBy.getDirection(), sortBy.getField());
        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);

        Specification<CustomerOrder> spec = Specification.where(
                CustomerOrderSpecification.hasStatus(status))
            .and(CustomerOrderSpecification.hasBuyerId(buyerId))
            .and((CustomerOrderSpecification.isCreatedBetween(startDate, endDate)));

        return getOrderHistoryResponse(spec, pageable);
    }

    @Override
    public OrderHistoryResponse getOrderDetailsForSeller(int pageNo, int pageSize, ProductSort sortBy, String status, UUID sellerId, Date startDate, Date endDate) {
        Sort sort = Sort.by(sortBy.getDirection(), sortBy.getField());
        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);

        Specification<CustomerOrder> spec = Specification.where(
                CustomerOrderSpecification.hasStatus(status))
            .and((CustomerOrderSpecification.hasSellerId(sellerId))
                .and((CustomerOrderSpecification.isCreatedBetween(startDate, endDate)))
            );

        return getOrderHistoryResponse(spec, pageable);
    }

    private OrderHistoryResponse getOrderHistoryResponse(Specification<CustomerOrder> spec, Pageable pageable) {
        Map<Shop, List<CustomerOrder>> shopOrderMap = new HashMap<>();
        Map<CustomerOrder, List<OrderDetail>> orderDetailMap = new HashMap<>();
        Page<CustomerOrder> customerOrders = customerOrderRepository.findAll(spec, pageable);
        List<CustomerOrder> customerOrderList = customerOrders.getContent();

        //Duyet qua tung order
        customerOrderList.forEach(customerOrder -> {

            //Set OrderDetail
            List<OrderDetail> orderDetails = (orderDetailRepository.findByOrderId(customerOrder.getId())
                .orElseThrow(() -> new ILinkException(HttpStatus.INTERNAL_SERVER_ERROR, "Request fails. Order detail not found")));

            orderDetails.forEach(
                orderDetail -> {

                    //Set Shop
                    orderDetail.getProduct().setShop(
                        shopRepository.findById(orderDetail.getProduct().getShop().getShopId())
                            .orElseThrow(() -> new ILinkException(HttpStatus.INTERNAL_SERVER_ERROR, "Request fails. Shop not found"))
                    );
                    //Set Order cho tung shop
                    if (shopOrderMap.containsKey(orderDetail.getProduct().getShop())) {
                        shopOrderMap.get(orderDetail.getProduct().getShop()).add(customerOrder);
                    } else {
                        List<CustomerOrder> orders = new ArrayList<>();
                        orders.add(customerOrder);
                        shopOrderMap.put(orderDetail.getProduct().getShop(), orders);
                    }
                }
            );


            if (orderDetailMap.containsKey(customerOrder)) {
                orderDetailMap.get(customerOrder).addAll(orderDetails);
            } else {
                orderDetailMap.put(customerOrder, orderDetails);
            }

        });

        List<OrderHistoryElement> orderHistoryElements = new ArrayList<>();

        shopOrderMap.forEach((shop, orders) -> {
            orders.forEach(order -> {

                List<OrderProductDTO> productDTOList = new ArrayList<>();

                if (orderDetailMap.containsKey(order)) {
                    List<OrderDetail> orderDetails = orderDetailMap.get(order);

                    // Sử dụng stream để xử lý dữ liệu
                    orderDetails.stream()
                        .map(orderDetail -> OrderProductDTO.builder()
                            .productId(orderDetail.getProduct().getId())
                            .productName(orderDetail.getProduct().getProductName())
                            .quantity(orderDetail.getQuantity())
                            .unitPrice(orderDetail.getPrice())
                            .lineTotal(orderDetail.getLineTotal())
                            .image(orderDetail.getProduct().getImage())
                            .build())
                        .forEach(productDTOList::add);

                    // Loại bỏ key sau khi xử lý xong
//                    orderDetailMap.remove(order);
                }

                orderHistoryElements.add(OrderHistoryElement.builder()
                    .id(order.getId())
                    .orderCode(order.getCode())
                    .buyerId(order.getAccount().getAccountId())
                    .buyerName(order.getAccount().fullName())
                    .customerName(order.getCustomerName())
                    .address(order.getAddress())
                    .description(order.getDescription())
                    .totalPrice(order.getTotalPrice())
                    .status(order.getStatus())
                    .createdDate(order.getCreatedDate().toString())
                    .productDTOList(productDTOList)
                    .shopId(shop.getShopId())
                    .shopName(shop.getShopName())
                    .paymentId(order.getTransaction().getPaymentId())
                    .paymentCode(order.getTransaction().getPaymentCode())
                    .paymentMethod(order.getTransaction().getPaymentMethod())
                    .build());
            });
        });


        return OrderHistoryResponse.builder()
            .content(orderHistoryElements)
            .totalPages(customerOrders.getTotalPages())
            .totalElements(customerOrders.getTotalElements())
            .pageSize(customerOrders.getSize())
            .pageNo(customerOrders.getNumber())
            .last(customerOrders.isLast())
            .build();
    }

}
