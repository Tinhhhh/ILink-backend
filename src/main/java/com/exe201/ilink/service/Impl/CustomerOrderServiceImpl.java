package com.exe201.ilink.service.Impl;

import com.exe201.ilink.config.converter.GenericConverter;
import com.exe201.ilink.model.entity.Account;
import com.exe201.ilink.model.entity.CustomerOrder;
import com.exe201.ilink.model.entity.Role;
import com.exe201.ilink.model.enums.RoleName;
import com.exe201.ilink.model.exception.ILinkException;
import com.exe201.ilink.model.payload.request.OrderInfo;
import com.exe201.ilink.repository.AccountRepository;
import com.exe201.ilink.repository.CustomerOrderRepository;
import com.exe201.ilink.service.CustomerOrderService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.config.Configuration;
import org.modelmapper.convention.MatchingStrategies;
import org.modelmapper.spi.MatchingStrategy;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomerOrderServiceImpl implements CustomerOrderService {

    private final AccountRepository accountRepository;
    private final CustomerOrderRepository orderRepository;
    private final GenericConverter<CustomerOrder> orderInfoConverter;
    private final ModelMapper modelMapper;

    @Override
    public void saveOrder(OrderInfo orderInfo) {
        Account account = accountRepository.findById(orderInfo.getAccountId())
            .orElseThrow(() -> new ILinkException(HttpStatus.INTERNAL_SERVER_ERROR,"Request fails. Account not found"));

        if (!RoleName.contains(account.getRole().getRoleName())){
            throw new ILinkException(HttpStatus.INTERNAL_SERVER_ERROR,"Request fails. Role is not valid");
        }

        CustomerOrder order = orderInfoConverter.toEntity(orderInfo, CustomerOrder.class);
        order.setAccount(account);


    }
}
