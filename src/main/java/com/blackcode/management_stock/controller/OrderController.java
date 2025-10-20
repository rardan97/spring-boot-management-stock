package com.blackcode.management_stock.controller;

import com.blackcode.management_stock.dto.OrderReq;
import com.blackcode.management_stock.dto.OrderRes;
import com.blackcode.management_stock.service.OrderService;
import com.blackcode.management_stock.utils.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@Validated
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<OrderRes>>> getOrderListAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size){
        Page<OrderRes> orderResList = orderService.getAllOrders(page, size);
        return ResponseEntity.ok(ApiResponse.success("Order retrieved successfully", 200, orderResList));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderRes>> getOrderFindById(@PathVariable("id") String id){
        OrderRes orderRes = orderService.getOrderById(id);
        return ResponseEntity.ok(ApiResponse.success("Order found",200, orderRes));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<OrderRes>> addOrder(@Valid @RequestBody OrderReq orderReq){
        OrderRes orderRes = orderService.createOrder(orderReq);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Order created successfully", 201, orderRes));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderRes>> updateOrder(@PathVariable("id") String id, @Valid @RequestBody OrderReq orderReq){
        OrderRes orderRes = orderService.updateOrder(id, orderReq);
        return ResponseEntity.ok(ApiResponse.success("Order Update successfully", 200, orderRes));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> deleteOrderById(@PathVariable("id") String id){
        Map<String, Object> rtn = orderService.deleteOrder(id);
        return ResponseEntity.ok(ApiResponse.success("Order deleted successfully", 200, rtn));
    }
}
