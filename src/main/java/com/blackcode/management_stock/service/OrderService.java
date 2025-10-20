package com.blackcode.management_stock.service;

import com.blackcode.management_stock.dto.OrderReq;
import com.blackcode.management_stock.dto.OrderRes;
import org.springframework.data.domain.Page;

import java.util.Map;

public interface OrderService {

    Page<OrderRes> getAllOrders(int page, int size);

    OrderRes getOrderById(String orderId);

    OrderRes createOrder(OrderReq orderReq);

    OrderRes updateOrder(String orderId, OrderReq orderReq);

    Map<String, Object> deleteOrder(String orderId);


}
