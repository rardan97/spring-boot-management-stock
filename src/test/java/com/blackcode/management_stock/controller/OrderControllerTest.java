package com.blackcode.management_stock.controller;

import com.blackcode.management_stock.dto.ItemDto;
import com.blackcode.management_stock.dto.OrderReq;
import com.blackcode.management_stock.dto.OrderRes;
import com.blackcode.management_stock.exception.DataNotFoundException;
import com.blackcode.management_stock.exception.GlobalExceptionHandler;
import com.blackcode.management_stock.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.*;

import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


public class OrderControllerTest {

    private MockMvc mockMvc;

    @Mock
    private OrderService orderService;

    private ObjectMapper objectMapper = new ObjectMapper();

    private OrderController orderController;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        orderController = new OrderController(orderService);

        mockMvc = MockMvcBuilders.standaloneSetup(orderController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void getOrderListAll_shouldReturnPagedItems() throws Exception {
        ItemDto item1 = new ItemDto(1L, "Item A", new BigDecimal(10000));
        ItemDto item2 = new ItemDto(2L, "Item B", new BigDecimal(20000));

        OrderRes order1 = new OrderRes("O001", item1, 2, new BigDecimal(20000));
        OrderRes order2 = new OrderRes("O002", item2, 2, new BigDecimal(40000));

        List<OrderRes> orderResList = Arrays.asList(order1, order2);
        Page<OrderRes> mockPage = new PageImpl<>(orderResList, PageRequest.of(0, 10), 2);

        when(orderService.getAllOrders(0, 10)).thenReturn(mockPage);
        mockMvc.perform(get("/api/orders")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("Order retrieved successfully"))
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.data.content[0].orderNo").value("O001"))
                .andExpect(jsonPath("$.data.content[0].orderQty").value(2))
                .andExpect(jsonPath("$.data.content[0].price").value(20000))
                .andExpect(jsonPath("$.data.content[0].item.itemId").value(1))
                .andExpect(jsonPath("$.data.content[0].item.itemName").value("Item A"))
                .andExpect(jsonPath("$.data.content[0].item.itemPrice").value(10000))
                .andExpect(jsonPath("$.data.content[1].orderNo").value("O002"))
                .andExpect(jsonPath("$.data.content[1].orderQty").value(2))
                .andExpect(jsonPath("$.data.content[1].price").value(40000))
                .andExpect(jsonPath("$.data.content[1].item.itemId").value(2))
                .andExpect(jsonPath("$.data.content[1].item.itemName").value("Item B"))
                .andExpect(jsonPath("$.data.content[1].item.itemPrice").value(20000));

    }

    @Test
    void getOrderById_shouldReturnOrder() throws Exception {
        ItemDto item = new ItemDto(1L, "Item A", new BigDecimal(10000));
        OrderRes orderRes = new OrderRes("O001", item, 2, new BigDecimal(20000));

        when(orderService.getOrderById("O001")).thenReturn(orderRes);
        mockMvc.perform(get("/api/orders/{id}", "O001")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("Order found"))
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.data.orderNo").value("O001"))
                .andExpect(jsonPath("$.data.orderQty").value(2))
                .andExpect(jsonPath("$.data.price").value(20000))
                .andExpect(jsonPath("$.data.item.itemId").value(1))
                .andExpect(jsonPath("$.data.item.itemName").value("Item A"))
                .andExpect(jsonPath("$.data.item.itemPrice").value(10000));
    }

    @Test
    void addOrder_shouldCreateAndReturnOrder() throws Exception {
        OrderReq orderReq = new OrderReq(1L, 10, new BigDecimal(10000));
        ItemDto item = new ItemDto(1L, "Item A", new BigDecimal(10000));
        OrderRes orderRes = new OrderRes("O001", item, 2, new BigDecimal(20000));

        when(orderService.createOrder(any(OrderReq.class))).thenReturn(orderRes);

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("Order created successfully"))
                .andExpect(jsonPath("$.statusCode").value(201))
                .andExpect(jsonPath("$.data.orderNo").value("O001"))
                .andExpect(jsonPath("$.data.orderQty").value(2))
                .andExpect(jsonPath("$.data.price").value(20000))
                .andExpect(jsonPath("$.data.item.itemId").value(1))
                .andExpect(jsonPath("$.data.item.itemName").value("Item A"))
                .andExpect(jsonPath("$.data.item.itemPrice").value(10000));
    }

    @Test
    void updateOrder_shouldUpdateAndReturnOrder() throws Exception {
        OrderReq orderReq = new OrderReq(1L, 3, new BigDecimal(30000));
        ItemDto item = new ItemDto(1L, "Item A", new BigDecimal(10000));
        OrderRes orderRes = new OrderRes("O001", item, 3, new BigDecimal(30000));

        when(orderService.updateOrder(eq("O001"), any(OrderReq.class))).thenReturn(orderRes);

        mockMvc.perform(put("/api/orders/{id}", "O001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("Order Update successfully"))
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.data.orderNo").value("O001"))
                .andExpect(jsonPath("$.data.orderQty").value(3))
                .andExpect(jsonPath("$.data.price").value(30000))
                .andExpect(jsonPath("$.data.item.itemId").value(1))
                .andExpect(jsonPath("$.data.item.itemName").value("Item A"))
                .andExpect(jsonPath("$.data.item.itemPrice").value(10000));
    }

    @Test
    void deleteOrderById_shouldReturnSuccessMessage() throws Exception {
        Map<String, Object> result = new HashMap<>();
        result.put("deletedOrderId", "O001");
        result.put("message", "Order successfully deleted and stock has been restored.");

        when(orderService.deleteOrder("O001")).thenReturn(result);

        mockMvc.perform(delete("/api/orders/{id}", "O001")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("Order deleted successfully"))
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.data.deletedOrderId").value("O001"))
                .andExpect(jsonPath("$.data.message").value("Order successfully deleted and stock has been restored."));
    }

//    ===============================

    @Test
    void getOrderById_shouldReturnNotFound_whenItemDoesNotExist() throws Exception {
        when(orderService.getOrderById("O999")).thenThrow(new DataNotFoundException("Order not found with id: O999"));

        mockMvc.perform(get("/api/orders/{id}", "O999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.statusCode").value(404))
                .andExpect(jsonPath("$.message").value("Order not found with id: O999"))
                .andExpect(jsonPath("$.data", nullValue()));
    }

    @Test
    void addOrder_shouldFailValidation_whenAllFieldsInvalid() throws Exception {
        OrderReq invalidOrderReq = new OrderReq(null, -999, null);

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidOrderReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.data.itemId").value("Item ID tidak boleh kosong"))
                .andExpect(jsonPath("$.data.orderQty").value("Quantity harus lebih dari 0"))
                .andExpect(jsonPath("$.data.price").value("Price tidak boleh kosong"));
    }

    @Test
    void addOrder_shouldFailValidation_whenItemIdIsNull() throws Exception {

        OrderReq invalidOrderReq = new OrderReq(null, 1, new BigDecimal(10000));
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidOrderReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("statusCode").value(400))
                .andExpect(jsonPath("$.data.itemId").value("Item ID tidak boleh kosong"));
    }

    @Test
    void addItem_shouldFailValidation_whenOrderQtyValid() throws Exception {
        OrderReq invalidOrderReq = new OrderReq(1L, -999, new BigDecimal(10000));

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidOrderReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.data.orderQty").value("Quantity harus lebih dari 0"));
    }

    @Test
    void addOrder_shouldFailValidation_whenPriceIsNull() throws Exception {
        OrderReq invalidOrderReq = new OrderReq(1L, 2, null);

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidOrderReq)))
                .andExpect(status().isBadRequest())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.data.price").value("Price tidak boleh kosong"));
    }

    @Test
    void updateOrder_shouldFailValidation_whenFieldsAreInvalid() throws Exception {
        OrderReq invalidOrderReq = new OrderReq(null, -999, null);

        mockMvc.perform(put("/api/orders/{id}", "O001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidOrderReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.data.itemId").value("Item ID tidak boleh kosong"))
                .andExpect(jsonPath("$.data.orderQty").value("Quantity harus lebih dari 0"))
                .andExpect(jsonPath("$.data.price").value("Price tidak boleh kosong"));
    }

    @Test
    void deleteOrder_shouldReturnNotFound_whenItemDoesNotExist() throws  Exception {
        when(orderService.deleteOrder("O999")).thenThrow(new DataNotFoundException("Order with ID O999 Not Found"));

        mockMvc.perform(delete("/api/orders/{id}", "O999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.statusCode").value(404))
                .andExpect(jsonPath("$.message").value("Order with ID O999 Not Found"))
                .andExpect(jsonPath("$.data", nullValue()));

    }

}
