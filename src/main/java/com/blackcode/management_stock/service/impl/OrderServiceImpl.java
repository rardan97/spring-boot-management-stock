package com.blackcode.management_stock.service.impl;

import com.blackcode.management_stock.dto.ItemDto;
import com.blackcode.management_stock.dto.OrderReq;
import com.blackcode.management_stock.dto.OrderRes;
import com.blackcode.management_stock.exception.DataNotFoundException;
import com.blackcode.management_stock.exception.InvalidPriceException;
import com.blackcode.management_stock.exception.NotEnoughStockException;
import com.blackcode.management_stock.model.Item;
import com.blackcode.management_stock.model.Order;
import com.blackcode.management_stock.repository.ItemRepository;
import com.blackcode.management_stock.repository.OrderRepository;
import com.blackcode.management_stock.service.OrderService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
public class OrderServiceImpl implements OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);

    private final OrderRepository orderRepository;

    private final ItemRepository itemRepository;

    public OrderServiceImpl(OrderRepository orderRepository, ItemRepository itemRepository) {
        this.orderRepository = orderRepository;
        this.itemRepository = itemRepository;
    }

    @Override
    public Page<OrderRes> getAllOrders(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Order> orderList = orderRepository.findAll(pageable);
        return orderList.map(this::mapToOrderRes);
    }

    @Override
    public OrderRes getOrderById(String orderId) {
        Order category = orderRepository.findById(orderId)
                .orElseThrow(() -> new DataNotFoundException("Order not found with id: "+orderId));
        return mapToOrderRes(category);
    }

    @Override
    @Transactional
    public OrderRes createOrder(OrderReq orderReq) {
        Order lastOrder = orderRepository.findLastOrderNative();
        String orderId = generateNextOrderId(lastOrder);

        Item item = itemRepository.findById(orderReq.getItemId())
                .orElseThrow(() -> new DataNotFoundException("Item not found"));

        BigDecimal totalPrice = item.getItemPrice().multiply(BigDecimal.valueOf(orderReq.getOrderQty()));

        if (orderReq.getPrice().compareTo(totalPrice) != 0) {
            throw new InvalidPriceException("Harga tidak valid. Harap jangan memanipulasi harga.");
        }

        reduceStock(item, orderReq.getOrderQty());

        Order order = new Order();
        order.setOrderNo(orderId);
        order.setOrderQty(orderReq.getOrderQty());
        order.setItem(item);
        order.setPrice(totalPrice);
        Order savedOrder = orderRepository.save(order);
        return mapToOrderRes(savedOrder);
    }

    @Override
    @Transactional
    public OrderRes updateOrder(String orderId, OrderReq orderReq) {
        Order existingOrder = orderRepository.findById(orderId)
                .orElseThrow(() -> new DataNotFoundException("Order with ID " + orderId + " Not Found"));

        Item newItem = itemRepository.findById(orderReq.getItemId())
                .orElseThrow(() -> new DataNotFoundException("Item not found"));

        int oldQty = existingOrder.getOrderQty();
        int newQty = orderReq.getOrderQty();
        Item oldItem = existingOrder.getItem();

        if (!oldItem.getItemId().equals(newItem.getItemId())) {
            restoreStock(oldItem, oldQty);
            reduceStock(newItem, newQty);
        } else {
            int diffQty = newQty - oldQty;
            if (diffQty > 0) {
                reduceStock(newItem, diffQty);
            } else if (diffQty < 0) {
                restoreStock(newItem, -diffQty);
            }
        }

        BigDecimal totalPrice = newItem.getItemPrice().multiply(BigDecimal.valueOf(newQty));

        if (orderReq.getPrice().compareTo(totalPrice) != 0) {
            throw new InvalidPriceException("Harga tidak valid. Harap jangan memanipulasi harga.");
        }

        existingOrder.setOrderQty(newQty);
        existingOrder.setItem(newItem);
        existingOrder.setPrice(totalPrice);

        Order updatedOrder = orderRepository.save(existingOrder);
        return mapToOrderRes(updatedOrder);
    }

    @Override
    @Transactional
    public Map<String, Object> deleteOrder(String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new DataNotFoundException("Order with ID " + orderId + " Not Found"));

        Item item = order.getItem();
        if (item != null) {
            restoreStock(item, order.getOrderQty());
        }

        orderRepository.delete(order);

        log.info("Order [{}] deleted. Item: {}, Qty: {}, Stock restored to: {}",
                orderId, item.getItemName(), order.getOrderQty(), item.getItemStock());

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("deletedOrderId", orderId);
        responseData.put("message", "Order successfully deleted and stock has been restored.");
        return responseData;
    }

    private String generateNextOrderId(Order lastOrder) {
        int newOrderNumber = 1;
        if (lastOrder != null) {
            String lastOrderNo = lastOrder.getOrderNo();
            String numberPart = lastOrderNo.substring(1);
            try {
                newOrderNumber = Integer.parseInt(numberPart) + 1;
            } catch (NumberFormatException e) {
                newOrderNumber = 1;
            }
        }
        return String.format("O%03d", newOrderNumber);
    }

    private void reduceStock(Item item, int qty) {
        if (item.getItemStock() < qty) {
            throw new NotEnoughStockException("Stok tidak mencukupi");
        }
        item.setItemStock(item.getItemStock() - qty);
        itemRepository.save(item);
    }

    private void restoreStock(Item item, int qty) {
        item.setItemStock(item.getItemStock() + qty);
        itemRepository.save(item);
    }

    private OrderRes mapToOrderRes(Order order) {
        OrderRes orderRes = new OrderRes();
        orderRes.setOrderNo(order.getOrderNo());
        orderRes.setOrderQty(order.getOrderQty());
        orderRes.setPrice(order.getPrice());
        ItemDto itemDto = new ItemDto();
        itemDto.setItemId(order.getItem().getItemId());
        itemDto.setItemName(order.getItem().getItemName());
        itemDto.setItemPrice(order.getItem().getItemPrice());
        orderRes.setItem(itemDto);
        return orderRes;
    }
}
