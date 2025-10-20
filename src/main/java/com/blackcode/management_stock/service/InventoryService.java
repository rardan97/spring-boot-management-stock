package com.blackcode.management_stock.service;

import com.blackcode.management_stock.dto.InventoryReq;
import com.blackcode.management_stock.dto.InventoryRes;
import org.springframework.data.domain.Page;

import java.util.Map;

public interface InventoryService {

    Page<InventoryRes> getAllInventory(int page, int size);

    InventoryRes getInventoryById(Long inventoryId);

    InventoryRes createInventory(InventoryReq inventoryReq);

    InventoryRes updateInventory(Long inventoryId, InventoryReq inventoryReq);

    Map<String, Object> deleteInventory(Long inventoryId);
}
