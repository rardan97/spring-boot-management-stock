package com.blackcode.management_stock.controller;

import com.blackcode.management_stock.dto.ItemReq;
import com.blackcode.management_stock.dto.ItemRes;
import com.blackcode.management_stock.service.ItemService;
import com.blackcode.management_stock.utils.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/items")
@Validated
public class ItemController {

    private final ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ItemRes>>> getItemListAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size){
        Page<ItemRes> itemResList = itemService.getAllItems(page, size);
        return ResponseEntity.ok(ApiResponse.success("Item retrieved successfully", 200, itemResList));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ItemRes>> getItemFindById(@PathVariable("id") Long id){
        ItemRes itemRes = itemService.getItemById(id);
        return ResponseEntity.ok(ApiResponse.success("Item found",200, itemRes));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ItemRes>> addItem(@Valid  @RequestBody ItemReq itemReq){
        ItemRes itemRes = itemService.createItem(itemReq);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Item created successfully", 201, itemRes));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ItemRes>> updateItem(@PathVariable("id") Long id, @Valid @RequestBody ItemReq itemReq){
        ItemRes itemRes = itemService.updateItem(id, itemReq);
        return ResponseEntity.ok(ApiResponse.success("Item updated successfully", 200, itemRes));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> deleteItemById(@PathVariable("id") Long id){
        Map<String, Object> rtn = itemService.deleteItem(id);
        return ResponseEntity.ok(ApiResponse.success("Item deleted successfully", 200, rtn));
    }

}
