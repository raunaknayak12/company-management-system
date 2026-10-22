package com.company.cms.repository;

import com.company.cms.entity.OrderStatus;
import com.company.cms.entity.SalesOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SalesOrderRepository extends JpaRepository<SalesOrder, Long> {
    Optional<SalesOrder> findByOrderNumber(String orderNumber);
    boolean existsByOrderNumber(String orderNumber);
    List<SalesOrder> findBySalesRepId(Long employeeId);
    List<SalesOrder> findByCustomerId(Long customerId);
    List<SalesOrder> findByStatus(OrderStatus status);
    List<SalesOrder> findByRegion(String region);
}
