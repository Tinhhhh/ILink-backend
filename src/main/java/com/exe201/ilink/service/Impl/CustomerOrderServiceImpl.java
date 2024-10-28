package com.exe201.ilink.service.Impl;

import com.exe201.ilink.Util.CustomerOrderSpecification;
import com.exe201.ilink.Util.DateUtil;
import com.exe201.ilink.Util.ProductSpecification;
import com.exe201.ilink.model.entity.*;
import com.exe201.ilink.model.enums.*;
import com.exe201.ilink.model.exception.ILinkException;
import com.exe201.ilink.model.payload.dto.OrderProductDTO;
import com.exe201.ilink.model.payload.request.OrderInfo;
import com.exe201.ilink.model.payload.response.OrderHistoryElement;
import com.exe201.ilink.model.payload.response.OrderHistoryResponse;
import com.exe201.ilink.model.payload.response.RegistrationInfoResponse;
import com.exe201.ilink.model.payload.response.SaleInfoResponse;
import com.exe201.ilink.repository.*;
import com.exe201.ilink.service.CustomerOrderService;
import com.exe201.ilink.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.config.Configuration;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Value;
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
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

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

    @Value("${application.frontend.url}")
    private String checkoutUrl;

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
    public void updateOrder(String paymentId, String paymentCode, String paymentStatus, String orderCode, boolean cancel, HttpServletResponse response) {

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
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                String formattedDateTime = customerOrder.getCreatedDate().toString().substring(0, 19);
                // Parse chuỗi thành LocalDateTime
                LocalDateTime dateTime = LocalDateTime.parse(formattedDateTime, formatter);

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
                        buyerAccount.getEmail(),
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

//        return PaymentStatementResponse.builder()
//            .paymentId(paymentId)
//            .paymentCode(paymentCode)
//            .paymentStatus(paymentStatus)
//            .orderCode(orderCode)
//            .cancel(cancel)
//            .build();
        if (!paymentStatus.equals(PaymentStatus.PAID.name())) {
            checkoutUrl += "/#/paymentError";
        } else {
            checkoutUrl += "/#/paymentSuccess";
        }
        response.setHeader("Location", checkoutUrl);
        response.setStatus(302);
    }

    @Override
    public OrderHistoryResponse getOrderDetailsForAdmin(int pageNo, int pageSize, ProductSort sortBy, String status, UUID sellerId, UUID buyerId, Date startDate, Date endDate) {
        Sort sort = Sort.by(sortBy.getDirection(), sortBy.getField());
        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);

        Date[] dates = adjustedDate(startDate, endDate);

        Specification<CustomerOrder> spec = Specification.where(
                CustomerOrderSpecification.hasStatus(status))
            .and(CustomerOrderSpecification.hasBuyerId(buyerId))
            .and((CustomerOrderSpecification.hasSellerId(sellerId))
                .and((CustomerOrderSpecification.isCreatedBetween(dates[0], dates[1])))
            );

        return getOrderHistoryResponse(spec, pageable);
    }

    @Override
    public OrderHistoryResponse getOrderDetailsForBuyer(int pageNo, int pageSize, ProductSort sortBy, String status, UUID buyerId, Date startDate, Date endDate) {
        Sort sort = Sort.by(sortBy.getDirection(), sortBy.getField());
        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);


        Date[] dates = adjustedDate(startDate, endDate);

        Specification<CustomerOrder> spec = Specification.where(
                CustomerOrderSpecification.hasStatus(status))
            .and(CustomerOrderSpecification.hasBuyerId(buyerId))
            .and((CustomerOrderSpecification.isCreatedBetween(dates[0], dates[1])));

        return getOrderHistoryResponse(spec, pageable);
    }

    @Override
    public OrderHistoryResponse getOrderDetailsForSeller(int pageNo, int pageSize, ProductSort sortBy, String status, UUID sellerId, Date startDate, Date endDate) {
        Sort sort = Sort.by(sortBy.getDirection(), sortBy.getField());
        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);


        Date[] dates = adjustedDate(startDate, endDate);

        Specification<CustomerOrder> spec = Specification.where(
                CustomerOrderSpecification.hasStatus(status))
            .and((CustomerOrderSpecification.hasSellerId(sellerId))
                .and((CustomerOrderSpecification.isCreatedBetween(dates[0], dates[1])))
            );

        return getOrderHistoryResponse(spec, pageable);
    }

    @Override
    public RegistrationInfoResponse getRegistrationDetailsForAdmin(Date startDate, Date endDate) {
        RegistrationInfoResponse registrationInfoResponse = new RegistrationInfoResponse();

        Date[] dates = adjustedDate(startDate, endDate);

        //Set total customers
        List<Account> accounts = accountRepository.findByCreatedDateBetween(dates[0], dates[1]);
        registrationInfoResponse.setTotalCustomers(accounts.size());

        //Set total products
        List<Product> products = productRepository.findByCreatedDateBetween(dates[0], dates[1]);
        registrationInfoResponse.setTotalProducts(products.size());

        //Set total sales
        List<CustomerOrder> orders = customerOrderRepository.findByCreatedDateBetween(dates[0], dates[1]);

        int total = orders.stream()
            .filter(customerOrder -> customerOrder.getStatus().equals(PaymentStatus.PAID.name()))
            .mapToInt(CustomerOrder::getTotalPrice)
            .sum();

        registrationInfoResponse.setTotalSales(total);

        //Set total transactions
        int transaction = (int) orders.stream()
            .filter(customerOrder -> customerOrder.getStatus().equals(PaymentStatus.PAID.name()))
            .count();

        registrationInfoResponse.setTotalTransactions(transaction);

        //set total commission
        registrationInfoResponse.setTotalCommission(total * 0.05);

        //Set percentage changes
        LocalDate[] localDates = DateUtil.getPreviousMonthRange(dates[0], dates[1]);
        Date previousStart = DateUtil.toDate(localDates[0]);
        Date previousEnd = DateUtil.toDate(localDates[1]);

        //Set percentage total customers
        List<Account> previousAccounts = accountRepository.findByCreatedDateBetween(previousStart, previousEnd);
        registrationInfoResponse.setCustomersPercentageChanges(calculatePercentageChange(previousAccounts.size(), accounts.size()));

        //Set percentage total products
        List<Product> previousProducts = productRepository.findByCreatedDateBetween(previousStart, previousEnd);
        registrationInfoResponse.setProductPercentageChanges(calculatePercentageChange(previousProducts.size(), products.size()));

        //Set percentage total sales
        List<CustomerOrder> previousOrders = customerOrderRepository.findByCreatedDateBetween(previousStart, previousEnd);
        int previousTotal = previousOrders.stream()
            .filter(customerOrder -> customerOrder.getStatus().equals(PaymentStatus.PAID.name()))
            .mapToInt(CustomerOrder::getTotalPrice).sum();

        registrationInfoResponse.setSalePercentageChanges(calculatePercentageChange(previousTotal, total));

        //Set percentage total transactions
        int previousTransaction = (int) previousOrders.stream()
            .filter(customerOrder -> customerOrder.getStatus().equals(PaymentStatus.PAID.name()))
            .count();

        registrationInfoResponse.setTransactionPercentageChanges(calculatePercentageChange(previousTransaction, transaction));

        //Set percentage total commission
        registrationInfoResponse.setCommissionPercentageChanges(calculatePercentageChange((int) (previousTotal * 0.05), (int) (total * 0.05)));

        return registrationInfoResponse;
    }

    @Override
    public SaleInfoResponse getSalesDetailsForSeller(Date startDate, Date endDate, UUID sellerId) {

        if (sellerId == null) {
            throw new ILinkException(HttpStatus.INTERNAL_SERVER_ERROR, "Request fails. SellerId is required");
        }

        SaleInfoResponse saleInfoResponse = new SaleInfoResponse();

        Date[] dates = adjustedDate(startDate, endDate);

        Specification<CustomerOrder> spec = Specification.where(
            CustomerOrderSpecification.hasSellerId(sellerId)
                .and((CustomerOrderSpecification.isCreatedBetween(dates[0], dates[1])
                )));

        List<CustomerOrder> orders = customerOrderRepository.findAll(spec);

        //Set total products
        List<Product> products = productRepository.findByCreatedDateBetween(dates[0], dates[1]);
        saleInfoResponse.setTotalProducts(products.size());

        //Set total sales
        orders.forEach(customerOrder -> {
            if (customerOrder.getStatus().equals(PaymentStatus.PAID.name())) {
                customerOrder.getOrderDetails().forEach(orderDetail -> {
                    if (orderDetail.getProduct().getShop().getAccount().getAccountId().equals(sellerId)) {
                        saleInfoResponse.setTotalSales(saleInfoResponse.getTotalSales() + orderDetail.getLineTotal());
                    }
                });
            }
        });

        //Set total net sales
        saleInfoResponse.setTotalNetSales(saleInfoResponse.getTotalSales() * 0.95);

        //Set pending products and cancelled products

        Shop shop = shopRepository.findByAccountId(sellerId)
            .orElseThrow(() -> new ILinkException(HttpStatus.INTERNAL_SERVER_ERROR, "Request fails. Shop not found"));

        Specification<Product> productSpec = Specification.where(
            ProductSpecification.hasShopId(shop.getShopId()
            ));

        List<Product> shopProducts = productRepository.findAll(productSpec);

        //Set pending products and cancelled products
        saleInfoResponse.setPendingProducts((int) shopProducts.stream()
            .filter(product -> product.getStatus().equals(ProductStatus.PENDING.getStatus()))
            .count());

        saleInfoResponse.setCancelledProducts((int) shopProducts.stream()
            .filter(product -> product.getStatus().equals(ProductStatus.REJECTED.getStatus()))
            .count());


        //Set percentage changes
        LocalDate[] localDates = DateUtil.getPreviousMonthRange(dates[0], dates[1]);
        Date previousStart = DateUtil.toDate(localDates[0]);
        Date previousEnd = DateUtil.toDate(localDates[1]);

        //Set percentage total sales
        Specification<CustomerOrder> previousSpec = Specification.where(
            CustomerOrderSpecification.hasSellerId(sellerId)
                .and((CustomerOrderSpecification.isCreatedBetween(previousStart, previousEnd)
                )));

        List<CustomerOrder> previousOrders = customerOrderRepository.findAll(previousSpec);

        AtomicInteger previousTotal = new AtomicInteger();
        previousOrders.forEach(customerOrder -> {
            if (customerOrder.getStatus().equals(PaymentStatus.PAID.name())) {
                customerOrder.getOrderDetails().forEach(orderDetail -> {
                    if (orderDetail.getProduct().getShop().getAccount().getAccountId().equals(sellerId)) {
                        previousTotal.addAndGet(orderDetail.getLineTotal());
                    }
                });
            }
        });

        saleInfoResponse.setSalePercentageChanges(calculatePercentageChange(previousTotal.get(), saleInfoResponse.getTotalSales()));

        //Set percentage net sales
        double previousNetTotal = previousTotal.get() * 0.95;
        double netSalePercentageChanges = calculatePercentageChange((int) previousNetTotal, (int) saleInfoResponse.getTotalNetSales());
        saleInfoResponse.setNetSalePercentageChanges(netSalePercentageChanges);

        return saleInfoResponse;
    }

    private static Date[] adjustedDate(Date startDate, Date endDate) {

        if (startDate == null || endDate == null) {
            return new Date[]{null, null};
        }

        // Chuyển đổi startDate thành LocalDateTime và thiết lập thời gian là 00:00:00
        LocalDateTime startDateTime = startDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().atStartOfDay();

        // Chuyển đổi endDate thành LocalDateTime và thiết lập thời gian là 23:59:59
        LocalDateTime endDateTime = endDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().atTime(23, 59, 59);

        // Chuyển đổi LocalDateTime về Date
        Date adjustedStartDate = Date.from(startDateTime.atZone(ZoneId.systemDefault()).toInstant());
        Date adjustedEndDate = Date.from(endDateTime.atZone(ZoneId.systemDefault()).toInstant());

        return new Date[]{adjustedStartDate, adjustedEndDate};
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
//                    orderDetails.stream()
//                        .map(orderDetail -> OrderProductDTO.builder()
//                            .productId(orderDetail.getProduct().getId())
//                            .productName(orderDetail.getProduct().getProductName())
//                            .quantity(orderDetail.getQuantity())
//                            .unitPrice(orderDetail.getPrice())
//                            .lineTotal(orderDetail.getLineTotal())
//                            .image(orderDetail.getProduct().getImage())
//                            .build())
//                        .forEach(productDTOList::add);

                    orderDetails.forEach(
                        orderDetail -> {
                            if (orderDetail.getProduct().getShop().getShopId().equals(shop.getShopId())) {
                                productDTOList.add(OrderProductDTO.builder()
                                    .productId(orderDetail.getProduct().getId())
                                    .productName(orderDetail.getProduct().getProductName())
                                    .quantity(orderDetail.getQuantity())
                                    .unitPrice(orderDetail.getPrice())
                                    .lineTotal(orderDetail.getLineTotal())
                                    .image(orderDetail.getProduct().getImage())
                                    .build());
                            }
                        }
                    );

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

    private double calculatePercentageChange(int previous, int current) {

        if (previous == 0) {
            return 100;
        }

        return ((double) (current - previous) / previous) * 100;
    }

}
