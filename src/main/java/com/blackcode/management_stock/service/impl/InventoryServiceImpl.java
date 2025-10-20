package com.blackcode.management_stock.service.impl;

import com.blackcode.management_stock.dto.InventoryReq;
import com.blackcode.management_stock.dto.InventoryRes;
import com.blackcode.management_stock.dto.ItemRes;
import com.blackcode.management_stock.exception.DataNotFoundException;
import com.blackcode.management_stock.exception.InvalidStockException;
import com.blackcode.management_stock.exception.NotEnoughStockException;
import com.blackcode.management_stock.model.Inventory;
import com.blackcode.management_stock.model.InventoryType;
import com.blackcode.management_stock.model.Item;
import com.blackcode.management_stock.repository.InventoryRepository;
import com.blackcode.management_stock.repository.ItemRepository;
import com.blackcode.management_stock.service.InventoryService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class InventoryServiceImpl implements InventoryService {

    private static final Logger log = LoggerFactory.getLogger(InventoryServiceImpl.class);

    private final InventoryRepository inventoryRepository;

    private final ItemRepository itemRepository;

    public InventoryServiceImpl(InventoryRepository inventoryRepository, ItemRepository itemRepository) {
        this.inventoryRepository = inventoryRepository;
        this.itemRepository = itemRepository;
    }

    @Override
    public Page<InventoryRes> getAllInventory(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Inventory> inventoryList = inventoryRepository.findAll(pageable);
        return inventoryList.map(this::mapToInventoryRes);
    }

    @Override
    public InventoryRes getInventoryById(Long inventoryId) {
        Inventory inventory = inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new DataNotFoundException("Inventory not found with id: "+inventoryId));
        return mapToInventoryRes(inventory);
    }

    @Override
    @Transactional
    public InventoryRes createInventory(InventoryReq inventoryReq) {

        Item item = itemRepository.findById(inventoryReq.getItemId())
                .orElseThrow(() -> new DataNotFoundException("Item not found with id:" +inventoryReq.getItemId()));

        Inventory inventory = new Inventory();
        inventory.setItem(item);
        inventory.setInventoryQty(inventoryReq.getInventoryQty());

        InventoryType type = inventoryReq.getInventoryType();
        inventory.setInventoryType(type);

        if (inventoryReq.getInventoryType().equals(InventoryType.T)) {
            item.setItemStock(item.getItemStock() + inventory.getInventoryQty());
        } else if (type == InventoryType.W) {
            if (item.getItemStock() < inventory.getInventoryQty()) {
                throw new NotEnoughStockException("Stok tidak mencukupi untuk withdrawal");
            }
            item.setItemStock(item.getItemStock() - inventory.getInventoryQty());
        }

        log.info("Stock item [{}] updated. Type: {}, Qty: {}, New Stock: {}",
                item.getItemName(), type, inventory.getInventoryQty(), item.getItemStock());

        itemRepository.save(item);
        Inventory inventory1 = inventoryRepository.save(inventory);

        return mapToInventoryRes(inventory1);
    }

    @Override
    @Transactional
    public InventoryRes updateInventory(Long inventoryId, InventoryReq inventoryReq) {
        Inventory inventory = inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new DataNotFoundException("Inventory with ID " + inventoryId + " Not Found"));

        Item item = inventory.getItem();
        int oldQty = inventory.getInventoryQty();
        InventoryType oldType = inventory.getInventoryType();

        InventoryType newType = inventoryReq.getInventoryType();
        int newQty = inventoryReq.getInventoryQty();

        if (oldType == InventoryType.T) {
            item.setItemStock(item.getItemStock() - oldQty);
        } else if (oldType == InventoryType.W) {
            item.setItemStock(item.getItemStock() + oldQty);
        }

        if (newType == InventoryType.T) {
            item.setItemStock(item.getItemStock() + newQty);
        } else if (newType == InventoryType.W) {
            if (item.getItemStock() < newQty) {
                throw new NotEnoughStockException("Stok tidak mencukupi untuk withdrawal");
            }
            item.setItemStock(item.getItemStock() - newQty);
        }

        if (item.getItemStock() < 0) {
            throw new InvalidStockException("Stock cannot be negative");
        }

        itemRepository.save(item);

        inventory.setInventoryType(newType);
        inventory.setInventoryQty(newQty);
        Inventory updatedInventory = inventoryRepository.save(inventory);

        log.info("Stock item [{}] updated via inventory update. OldType: {}, OldQty: {}, NewType: {}, NewQty: {}, New Stock: {}",
                item.getItemName(), oldType, oldQty, newType, newQty, item.getItemStock());

        return mapToInventoryRes(updatedInventory);
    }

    @Override
    @Transactional
    public Map<String, Object> deleteInventory(Long inventoryId) {
        Inventory inventory = inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new DataNotFoundException("Inventory with ID "+inventoryId + "Not Found"));
        inventoryRepository.delete(inventory);
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("deletedInventoryId", inventoryId);
        responseData.put("info", "The inventory was removed from the database.");
        return responseData;
    }

    private InventoryRes mapToInventoryRes(Inventory inventory) {
        InventoryRes inventoryRes = new InventoryRes();
        ItemRes itemRes = new ItemRes();
        inventoryRes.setInventoryId(inventory.getInventoryId());
        inventoryRes.setInventoryType(inventory.getInventoryType().name());
        inventoryRes.setInventoryQty(inventory.getInventoryQty());
        itemRes.setItemId(inventory.getItem().getItemId());
        itemRes.setItemName(inventory.getItem().getItemName());
        itemRes.setItemPrice(inventory.getItem().getItemPrice());
        itemRes.setItemStock(inventory.getItem().getItemStock());
        inventoryRes.setItem(itemRes);
        return inventoryRes;
    }
}
