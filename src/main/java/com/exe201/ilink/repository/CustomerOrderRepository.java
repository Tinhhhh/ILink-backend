package com.exe201.ilink.repository;

import com.exe201.ilink.model.entity.CustomerOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerOrderRepository extends JpaRepository<CustomerOrder, Long>, JpaSpecificationExecutor<CustomerOrder> {
    Optional<CustomerOrder> findByCode(String orderCode);

    List<CustomerOrder> findByCreatedDateBetween(Date startDate, Date endDate);
}
