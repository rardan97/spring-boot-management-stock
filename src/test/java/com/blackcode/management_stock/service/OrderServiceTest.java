package com.blackcode.management_stock.service;

import com.blackcode.management_stock.dto.OrderReq;
import com.blackcode.management_stock.dto.OrderRes;
import com.blackcode.management_stock.exception.DataNotFoundException;
import com.blackcode.management_stock.exception.InvalidPriceException;
import com.blackcode.management_stock.exception.NotEnoughStockException;
import com.blackcode.management_stock.model.Item;
import com.blackcode.management_stock.model.Order;
import com.blackcode.management_stock.repository.ItemRepository;
import com.blackcode.management_stock.repository.OrderRepository;
import com.blackcode.management_stock.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private OrderServiceImpl orderService;

    private AutoCloseable closeable;

    @BeforeEach
    void setup() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    void getAllOrders_shouldReturnPagedOrders() {
        List<Order> orders = Collections.singletonList(new Order("O001", new Item(), 1, BigDecimal.TEN));
        Pageable pageable = PageRequest.of(0, 10);
        Page<Order> page = new PageImpl<>(orders, pageable, orders.size());

        when(orderRepository.findAll(pageable)).thenReturn(page);
        Page<OrderRes> result = orderService.getAllOrders(0, 10);
        assertEquals(1, result.getTotalElements());
        assertEquals("O001", result.getContent().get(0).getOrderNo());
    }

    @Test
    void getOrderById_shouldReturnOrder() {
        Order order = new Order("O001", new Item(), 2, new BigDecimal("20000"));

        when(orderRepository.findById("O001")).thenReturn(Optional.of(order));
        OrderRes res = orderService.getOrderById("O001");
        assertEquals("O001", res.getOrderNo());
    }

    @Test
    void createOrder_shouldCreateSuccessfully() {
        OrderReq req = new OrderReq(1L, 2, new BigDecimal("20000"));
        Item item = new Item(1L, "Item A", new BigDecimal("10000"), 10);
        Order lastOrder = new Order("O001", null, 0, BigDecimal.ZERO);

        when(orderRepository.findLastOrderNative()).thenReturn(lastOrder);
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArguments()[0]);

        OrderRes res = orderService.createOrder(req);

        assertNotNull(res);
        assertEquals("O002", res.getOrderNo());
        assertEquals(2, res.getOrderQty());
        assertEquals(new BigDecimal("20000"), res.getPrice());
        verify(itemRepository).save(any(Item.class));
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void createOrder_shouldThrow_whenItemNotFound() {
        OrderReq req = new OrderReq(999L, 1, new BigDecimal("10000"));

        when(orderRepository.findLastOrderNative()).thenReturn(null);
        when(itemRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(DataNotFoundException.class, () -> orderService.createOrder(req));
    }

    @Test
    void createOrder_shouldThrow_whenStockNotEnough() {
        OrderReq req = new OrderReq(1L, 100, new BigDecimal("1000000"));
        Item item = new Item(1L, "Item A", new BigDecimal("10000"), 5);

        when(orderRepository.findLastOrderNative()).thenReturn(null);
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        assertThrows(NotEnoughStockException.class, () -> orderService.createOrder(req));
    }

    @Test
    void createOrder_shouldThrow_whenPriceManipulated() {
        OrderReq req = new OrderReq(1L, 2, new BigDecimal("9999"));
        Item item = new Item(1L, "Item A", new BigDecimal("10000"), 10);

        when(orderRepository.findLastOrderNative()).thenReturn(null);
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        assertThrows(InvalidPriceException.class, () -> orderService.createOrder(req));
    }

    @Test
    void updateOrder_shouldUpdateAndAdjustStock() {
        Item item = new Item(1L, "Item A", new BigDecimal("10000"), 10);
        Order existingOrder = new Order("O001", item, 3, new BigDecimal("30000"));
        OrderReq req = new OrderReq(1L, 5, new BigDecimal("50000"));

        when(orderRepository.findById("O001")).thenReturn(Optional.of(existingOrder));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArguments()[0]);

        OrderRes res = orderService.updateOrder("O001", req);

        assertNotNull(res);
        assertEquals(5, res.getOrderQty());
        assertEquals(new BigDecimal("50000"), res.getPrice());
        verify(itemRepository, atLeastOnce()).save(any(Item.class));
    }

    @Test
    void deleteOrder_shouldRestoreStockAndDelete() {
        Item item = new Item(1L, "Item A", new BigDecimal("10000"), 5);
        Order order = new Order("O001", item, 3, new BigDecimal("30000"));

        when(orderRepository.findById("O001")).thenReturn(Optional.of(order));

        Map<String, Object> response = orderService.deleteOrder("O001");

        verify(orderRepository).delete(order);
        verify(itemRepository).save(item);
        assertEquals("O001", response.get("deletedOrderId"));
        assertEquals("Order successfully deleted and stock has been restored.", response.get("message"));
    }
}
