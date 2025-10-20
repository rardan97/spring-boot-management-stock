package com.blackcode.management_stock.controller;

import com.blackcode.management_stock.dto.InventoryReq;
import com.blackcode.management_stock.dto.InventoryRes;
import com.blackcode.management_stock.dto.ItemRes;
import com.blackcode.management_stock.exception.DataNotFoundException;
import com.blackcode.management_stock.exception.GlobalExceptionHandler;
import com.blackcode.management_stock.model.InventoryType;
import com.blackcode.management_stock.service.InventoryService;
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


public class InventoryControllerTest {
    private MockMvc mockMvc;

    @Mock
    private InventoryService inventoryService;

    private ObjectMapper objectMapper = new ObjectMapper();

    private InventoryController inventoryController;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        inventoryController = new InventoryController(inventoryService);
        mockMvc = MockMvcBuilders.standaloneSetup(inventoryController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void getInventoryListAll_shouldReturnPagedInventory() throws Exception {
        ItemRes item1 = new ItemRes(1L, "Item A", new BigDecimal("10000"), 10);
        ItemRes item2 = new ItemRes(2L, "Item B", new BigDecimal("20000"), 20);
        InventoryRes inventory1 = new InventoryRes(1L, item1, 10, "T");
        InventoryRes inventory2 = new InventoryRes(2L, item2, 15, "W");

        List<InventoryRes> inventoryRes = Arrays.asList(inventory1, inventory2);

        Page<InventoryRes> mockPage = new PageImpl<>(inventoryRes, PageRequest.of(0, 10), 2);
        when(inventoryService.getAllInventory(0, 10)).thenReturn(mockPage);

        mockMvc.perform(get("/api/inventory")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("Inventory retrieved successfully"))
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.data.content[0].inventoryId").value(1))
                .andExpect(jsonPath("$.data.content[0].inventoryQty").value(10))
                .andExpect(jsonPath("$.data.content[0].inventoryType").value("T"))
                .andExpect(jsonPath("$.data.content[0].item.itemId").value(1))
                .andExpect(jsonPath("$.data.content[0].item.itemName").value("Item A"))
                .andExpect(jsonPath("$.data.content[0].item.itemPrice").value(10000))
                .andExpect(jsonPath("$.data.content[0].item.itemStock").value(10))
                .andExpect(jsonPath("$.data.content[1].inventoryId").value(2))
                .andExpect(jsonPath("$.data.content[1].inventoryQty").value(15))
                .andExpect(jsonPath("$.data.content[1].inventoryType").value("W"))
                .andExpect(jsonPath("$.data.content[1].item.itemId").value(2))
                .andExpect(jsonPath("$.data.content[1].item.itemName").value("Item B"))
                .andExpect(jsonPath("$.data.content[1].item.itemPrice").value(20000))
                .andExpect(jsonPath("$.data.content[1].item.itemStock").value(20));
    }

    @Test
    void getInventoryFindById_shouldReturnInventory() throws Exception {
        ItemRes item = new ItemRes(1L, "Item A", new BigDecimal("10000"), 10);
        InventoryRes inventoryRes = new InventoryRes(1L, item, 15, "T");
        when(inventoryService.getInventoryById(1L)).thenReturn(inventoryRes);

        mockMvc.perform(get("/api/inventory/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("Inventory found"))
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.data.inventoryId").value(1))
                .andExpect(jsonPath("$.data.inventoryQty").value(15))
                .andExpect(jsonPath("$.data.inventoryType").value("T"))
                .andExpect(jsonPath("$.data.item.itemId").value(1))
                .andExpect(jsonPath("$.data.item.itemName").value("Item A"))
                .andExpect(jsonPath("$.data.item.itemPrice").value(10000))
                .andExpect(jsonPath("$.data.item.itemStock").value(10));

    }

    @Test
    void addInventory_shouldCreateAndReturnInventory() throws Exception {
        InventoryReq inventoryReq = new InventoryReq(1L, 20, InventoryType.T);

        ItemRes item = new ItemRes(1L, "Item A", new BigDecimal("10000"), 10);
        InventoryRes inventoryRes = new InventoryRes(1L, item, 20, "T");

        when(inventoryService.createInventory(any(InventoryReq.class))).thenReturn(inventoryRes);

        mockMvc.perform(post("/api/inventory")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inventoryReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("Inventory created successfully"))
                .andExpect(jsonPath("$.statusCode").value(201))
                .andExpect(jsonPath("$.data.inventoryId").value(1))
                .andExpect(jsonPath("$.data.inventoryQty").value(20))
                .andExpect(jsonPath("$.data.inventoryType").value("T"))
                .andExpect(jsonPath("$.data.item.itemId").value(1))
                .andExpect(jsonPath("$.data.item.itemName").value("Item A"))
                .andExpect(jsonPath("$.data.item.itemPrice").value(10000))
                .andExpect(jsonPath("$.data.item.itemStock").value(10));
    }

    @Test
    void updateInventory_shouldUpdateAndReturnInventory() throws Exception {
        InventoryReq inventoryReq = new InventoryReq(1L, 25, InventoryType.W);

        ItemRes item = new ItemRes(1L, "Item A", new BigDecimal("10000"), 10);
        InventoryRes inventoryRes = new InventoryRes(1L, item, 25, "W");

        when(inventoryService.updateInventory(eq(1L), any(InventoryReq.class))).thenReturn(inventoryRes);

        mockMvc.perform(put("/api/inventory/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inventoryReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("Inventory updated successfully"))
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.data.inventoryId").value(1))
                .andExpect(jsonPath("$.data.inventoryQty").value(25))
                .andExpect(jsonPath("$.data.inventoryType").value("W"))
                .andExpect(jsonPath("$.data.item.itemId").value(1))
                .andExpect(jsonPath("$.data.item.itemName").value("Item A"))
                .andExpect(jsonPath("$.data.item.itemPrice").value(10000))
                .andExpect(jsonPath("$.data.item.itemStock").value(10));
    }

    @Test
    void deleteInventoryById_shouldReturnSuccessMessage() throws Exception {
        Map<String, Object> result = new HashMap<>();

        result.put("deletedInventoryId", 1L);
        result.put("info", "The inventory was removed from the database.");

        when(inventoryService.deleteInventory(1L)).thenReturn(result);

        mockMvc.perform(delete("/api/inventory/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("Inventory deleted successfully"))
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.data.deletedInventoryId").value(1L))
                .andExpect(jsonPath("$.data.info").value("The inventory was removed from the database."));
    }


    @Test
    void getInventoryFindById_shouldReturnNotFound_whenInventoryDoesNotExist() throws Exception {
        when(inventoryService.getInventoryById(999L)).thenThrow(new DataNotFoundException("Inventory not found with id: 999"));

        mockMvc.perform(get("/api/inventory/{id}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("Inventory not found with id: 999"))
                .andExpect(jsonPath("$.statusCode").value(404))
                .andExpect(jsonPath("$.data", nullValue()));
    }



    @Test
    void addInventory_shouldFailValidation_whenFieldsInvalid() throws Exception {
        InventoryReq invalidReq = new InventoryReq(null, -999, null);

        mockMvc.perform(post("/api/inventory")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.data.itemId").value("Item ID harus diisi"))
                .andExpect(jsonPath("$.data.inventoryQty").value("Jumlah inventory harus minimal 1"))
                .andExpect(jsonPath("$.data.inventoryType").value("Tipe inventory harus diisi"));
    }

    @Test
    void addInventory_shouldFailValidation_whenItemIdNull() throws Exception {
        InventoryReq invalidReq = new InventoryReq(null, 1, InventoryType.T);

        mockMvc.perform(post("/api/inventory")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.data.itemId").value("Item ID harus diisi"));
    }

    @Test
    void addInventory_shouldFailValidation_whenInventoryQtyFail() throws Exception {
        InventoryReq invalidReq = new InventoryReq(1L, -999, InventoryType.T);

        mockMvc.perform(post("/api/inventory")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.data.inventoryQty").value("Jumlah inventory harus minimal 1"));
    }

    @Test
    void addInventory_shouldFailValidation_whenInventoryTypeNull() throws Exception {
        InventoryReq invalidReq = new InventoryReq(1L, 1, null);

        mockMvc.perform(post("/api/inventory")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.data.inventoryType").value("Tipe inventory harus diisi"));
    }

    @Test
    void updateInventory_shouldFailValidation_whenFieldsAreInvalid() throws Exception {
        InventoryReq invalidReq = new InventoryReq(null, -999, null);

        mockMvc.perform(put("/api/inventory/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.data.itemId").value("Item ID harus diisi"))
                .andExpect(jsonPath("$.data.inventoryQty").value("Jumlah inventory harus minimal 1"))
                .andExpect(jsonPath("$.data.inventoryType").value("Tipe inventory harus diisi"));
    }

    @Test
    void deleteInventory_shouldReturnNotFound_whenInventoryDoesNotExist() throws  Exception {
        when(inventoryService.deleteInventory(999L)).thenThrow(new DataNotFoundException("Inventory with ID 999 Not Found"));

        mockMvc.perform(delete("/api/inventory/{id}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.statusCode").value(404))
                .andExpect(jsonPath("$.message").value("Inventory with ID 999 Not Found"))
                .andExpect(jsonPath("$.data", nullValue()));

    }
}
