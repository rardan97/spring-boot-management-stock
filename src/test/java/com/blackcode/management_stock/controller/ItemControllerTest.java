package com.blackcode.management_stock.controller;

import com.blackcode.management_stock.dto.ItemReq;
import com.blackcode.management_stock.dto.ItemRes;
import com.blackcode.management_stock.exception.DataNotFoundException;
import com.blackcode.management_stock.exception.GlobalExceptionHandler;
import com.blackcode.management_stock.service.ItemService;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class ItemControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ItemService itemService;

    private ObjectMapper objectMapper = new ObjectMapper();

    private ItemController itemController;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        itemController = new ItemController(itemService);
        mockMvc = MockMvcBuilders.standaloneSetup(itemController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void getItemListAll_shouldReturnPagedItems() throws Exception {
        ItemRes item1 = new ItemRes(1L, "Item A", new BigDecimal("10000"), 10);
        ItemRes item2 = new ItemRes(2L, "Item B", new BigDecimal("50000"), 5);

        List<ItemRes> itemResList = Arrays.asList(item1, item2);
        Page<ItemRes> mockPage = new PageImpl<>(itemResList, PageRequest.of(0, 10), 2);

        when(itemService.getAllItems(0, 10)).thenReturn(mockPage);

        mockMvc.perform(get("/api/items")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("Item retrieved successfully"))
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.data.content[0].itemId").value(1))
                .andExpect(jsonPath("$.data.content[0].itemName").value("Item A"))
                .andExpect(jsonPath("$.data.content[0].itemPrice").value(10000))
                .andExpect(jsonPath("$.data.content[0].itemStock").value(10))
                .andExpect(jsonPath("$.data.content[1].itemId").value(2))
                .andExpect(jsonPath("$.data.content[1].itemName").value("Item B"))
                .andExpect(jsonPath("$.data.content[1].itemPrice").value(50000))
                .andExpect(jsonPath("$.data.content[1].itemStock").value(5));
    }

    @Test
    void getItemById_shouldReturnItem() throws Exception {
        ItemRes mockRes = new ItemRes(1L, "Item A", new BigDecimal("10000"), 10);

        when(itemService.getItemById(1L)).thenReturn(mockRes);

        mockMvc.perform(get("/api/items/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("Item found"))
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.data.itemId").value(1))
                .andExpect(jsonPath("$.data.itemName").value("Item A"))
                .andExpect(jsonPath("$.data.itemPrice").value(10000))
                .andExpect(jsonPath("$.data.itemStock").value(10));
    }

    @Test
    void addItem_shouldCreateAndReturnItem() throws Exception {
        ItemReq itemReq = new ItemReq("Item A", new BigDecimal("10000"), 10);

        ItemRes itemRes = new ItemRes(1L, "Item A", new BigDecimal("10000"), 10);

        when(itemService.createItem(any(ItemReq.class))).thenReturn(itemRes);

        mockMvc.perform(post("/api/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("Item created successfully"))
                .andExpect(jsonPath("$.statusCode").value(201))
                .andExpect(jsonPath("$.data.itemId").value(1))
                .andExpect(jsonPath("$.data.itemName").value("Item A"))
                .andExpect(jsonPath("$.data.itemPrice").value(10000))
                .andExpect(jsonPath("$.data.itemStock").value(10));
    }

    @Test
    void updateItem_shouldUpdateAndReturnItem() throws Exception {
        ItemReq itemReq = new ItemReq("Item A", new BigDecimal("20000"), 10);
        ItemRes itemRes = new ItemRes(1L, "Item A", new BigDecimal("20000"), 10);

        when(itemService.updateItem(eq(1L), any(ItemReq.class))).thenReturn(itemRes);

        mockMvc.perform(put("/api/items/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("Item updated successfully"))
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.data.itemId").value(1))
                .andExpect(jsonPath("$.data.itemName").value("Item A"))
                .andExpect(jsonPath("$.data.itemPrice").value(20000))
                .andExpect(jsonPath("$.data.itemStock").value(10));
    }

    @Test
    void deleteItem_shouldReturnSuccessMessage() throws Exception {
        Map<String, Object> result = new HashMap<>();
        result.put("deletedItemId", 1L);
        result.put("info", "The Item was removed from the database.");

        when(itemService.deleteItem(1L)).thenReturn(result);

        mockMvc.perform(delete("/api/items/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("Item deleted successfully"))
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.data.deletedItemId").value(1))
                .andExpect(jsonPath("$.data.info").value("The Item was removed from the database."));
    }


    @Test
    void getItemById_shouldReturnNotFound_whenItemDoesNotExist() throws Exception {
        when(itemService.getItemById(999L)).thenThrow(new DataNotFoundException("Item not found with id: 999"));

        mockMvc.perform(get("/api/items/{id}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.statusCode").value(404))
                .andExpect(jsonPath("$.message").value("Item not found with id: 999"))
                .andExpect(jsonPath("$.data", nullValue()));
    }

    @Test
    void addItem_shouldFailValidation_whenAllFieldsInvalid() throws Exception {
        ItemReq invalidItemReq = new ItemReq("", new BigDecimal("-1000"), 0);

        mockMvc.perform(post("/api/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidItemReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.data.itemName").value("Item name harus diisi"))
                .andExpect(jsonPath("$.data.itemPrice").value("Harga harus minimal 1"))
                .andExpect(jsonPath("$.data.itemStock").value("Stok tidak boleh kurang dari 1"));
    }

    @Test
    void addItem_shouldFailValidation_whenItemNameIsBlank() throws Exception {
        ItemReq invalidItemReq = new ItemReq("", new BigDecimal("10000"), 10);
        mockMvc.perform(post("/api/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidItemReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("statusCode").value(400))
                .andExpect(jsonPath("$.data.itemName").value("Item name harus diisi"));
    }

    @Test
    void addItem_shouldFailValidation_whenItemPriceIsNull() throws Exception {
        ItemReq invalidItemReq = new ItemReq("Item A", null, 10);

        mockMvc.perform(post("/api/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidItemReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.data.itemPrice").value("Item price harus diisi"));
    }

    @Test
    void addItem_shouldFailValidation_whenItemStockIsNull() throws Exception {
        ItemReq invalidItemReq = new ItemReq("Item A", new BigDecimal("10000"), null);

        mockMvc.perform(post("/api/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidItemReq)))
                .andExpect(status().isBadRequest())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.data.itemStock").value("Item stock harus diisi"));
    }

    @Test
    void updateItem_shouldFailValidation_whenFieldsAreInvalid() throws Exception {
        ItemReq invalidReq = new ItemReq("", new BigDecimal("-5000"), 0);

        mockMvc.perform(put("/api/items/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.data.itemName").value("Item name harus diisi"))
                .andExpect(jsonPath("$.data.itemPrice").value("Harga harus minimal 1"))
                .andExpect(jsonPath("$.data.itemStock").value("Stok tidak boleh kurang dari 1"));
    }

    @Test
    void deleteItem_shouldReturnNotFound_whenItemDoesNotExist() throws  Exception {
        when(itemService.deleteItem(999L)).thenThrow(new DataNotFoundException("Item with ID 999 Not Found"));

        mockMvc.perform(delete("/api/items/{id}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.statusCode").value(404))
                .andExpect(jsonPath("$.message").value("Item with ID 999 Not Found"))
                .andExpect(jsonPath("$.data", nullValue()));
    }

}
