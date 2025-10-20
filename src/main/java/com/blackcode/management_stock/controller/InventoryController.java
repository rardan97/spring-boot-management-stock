package com.blackcode.management_stock.controller;

import com.blackcode.management_stock.dto.InventoryReq;
import com.blackcode.management_stock.dto.InventoryRes;
import com.blackcode.management_stock.service.InventoryService;
import com.blackcode.management_stock.utils.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/inventory")
@Validated
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<InventoryRes>>> getInventoryListAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size){
        Page<InventoryRes> inventoryRes = inventoryService.getAllInventory(page, size);
        return ResponseEntity.ok(ApiResponse.success("Inventory retrieved successfully", 200, inventoryRes));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<InventoryRes>> getInventoryFindById(@PathVariable("id") Long id){
        InventoryRes inventoryRes = inventoryService.getInventoryById(id);
        return ResponseEntity.ok(ApiResponse.success("Inventory found",200, inventoryRes));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<InventoryRes>> addInventory(@Valid @RequestBody InventoryReq inventoryReq){
        InventoryRes inventoryRes = inventoryService.createInventory(inventoryReq);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Inventory created successfully", 201, inventoryRes));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<InventoryRes>> updateInventory(@PathVariable("id") Long id, @Valid @RequestBody InventoryReq inventoryReq){
        InventoryRes inventoryRes = inventoryService.updateInventory(id, inventoryReq);
        return ResponseEntity.ok(ApiResponse.success("Inventory updated successfully", 200, inventoryRes));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> deleteInventoryById(@PathVariable("id") Long id){
        Map<String, Object> rtn = inventoryService.deleteInventory(id);
        return ResponseEntity.ok(ApiResponse.success("Inventory deleted successfully", 200, rtn));
    }
}
