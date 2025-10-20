package com.blackcode.management_stock.repository;

import com.blackcode.management_stock.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {
    @Query(value = "SELECT * FROM tb_order ORDER BY CAST(SUBSTRING(order_no, 2) AS INT) DESC LIMIT 1", nativeQuery = true)
    Order findLastOrderNative();
}
