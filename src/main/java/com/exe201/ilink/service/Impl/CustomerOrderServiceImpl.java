package com.exe201.ilink.service.Impl;

import com.exe201.ilink.model.entity.*;
import com.exe201.ilink.model.enums.EmailTemplateName;
import com.exe201.ilink.model.enums.PaymentStatus;
import com.exe201.ilink.model.enums.ProductStatus;
import com.exe201.ilink.model.enums.RoleName;
import com.exe201.ilink.model.exception.ILinkException;
import com.exe201.ilink.model.payload.dto.OrderProductDTO;
import com.exe201.ilink.model.payload.request.OrderInfo;
import com.exe201.ilink.model.payload.response.PaymentStatementResponse;
import com.exe201.ilink.repository.*;
import com.exe201.ilink.service.CustomerOrderService;
import com.exe201.ilink.service.EmailService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.config.Configuration;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

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

//        List<Product> products = new ArrayList<>();
//
//        orderInfo.getProducts().forEach(
//            product ->
//                products.add(productRepository.findByProductId(product.getProductId())
//                    .orElseThrow(() -> new ILinkException(HttpStatus.INTERNAL_SERVER_ERROR, "Request fails. Product not found")))
//        );

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

        //Update Order Status
        customerOrder.setStatus(paymentStatus);
        customerOrderRepository.save(customerOrder);

        //Update Transaction
        Transaction transaction = customerOrder.getTransaction();
        transaction.setPaymentCode(paymentCode);
        transaction.setPaymentId(paymentId);
        transaction.setOrder(customerOrder);
        transactionRepository.save(transaction);

        //Send email to Buyer and Seller
        if (!paymentStatus.equals("CANCELLED") && paymentCode.equals("00") && !cancel) {

            List<OrderDetail> details = orderDetailRepository.findByOrderId(customerOrder.getId());
            //Send email to Seller
            List<Shop> shops = details.stream().map(orderDetail -> orderDetail.getProduct().getShop()).toList();

            //send email to each shop in Bill
            shops.forEach(shop -> {

                Account account = accountRepository.findById(shop.getAccount().getAccountId())
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


                try {
                    //Send email to Seller
                    emailService.sendMimeMessageForSeller(
                        account.getName(),
                        customerOrder.getCustomerName(),
                        account.getEmail(),
                        customerOrder.getCode(),
                        orderProductDTOS,
                        customerOrder.getTotalPrice(),
                        customerOrder.getAddress(),
                        EmailTemplateName.NEW_ORDER_INFORM.getName(),
                        "[Souvi] You have new order !");
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


}
