package com.blackcode.management_stock.service.impl;

import com.blackcode.management_stock.dto.ItemReq;
import com.blackcode.management_stock.dto.ItemRes;
import com.blackcode.management_stock.exception.DataNotFoundException;
import com.blackcode.management_stock.model.Item;
import com.blackcode.management_stock.repository.ItemRepository;
import com.blackcode.management_stock.service.ItemService;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;

    public ItemServiceImpl(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    @Override
    public Page<ItemRes> getAllItems(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Item> itemList = itemRepository.findAll(pageable);
        return itemList.map(this::mapToItemRes);
    }

    @Override
    public ItemRes getItemById(Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new DataNotFoundException("Item not found with id: "+itemId));
        return mapToItemRes(item);
    }

    @Override
    @Transactional
    public ItemRes createItem(ItemReq itemReq) {
        Item item = new Item();
        item.setItemName(itemReq.getItemName());
        item.setItemPrice(itemReq.getItemPrice());
        item.setItemStock(itemReq.getItemStock());
        Item savedItem = itemRepository.save(item);
        return mapToItemRes(savedItem);
    }

    @Override
    @Transactional
    public ItemRes updateItem(Long itemId, ItemReq itemReq) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new DataNotFoundException("Item with ID "+itemId + " Not Found"));

        item.setItemName(itemReq.getItemName());
        item.setItemPrice(itemReq.getItemPrice());
        item.setItemStock(itemReq.getItemStock());
        Item updatedItem = itemRepository.save(item);
        return mapToItemRes(updatedItem);
    }

    @Override
    @Transactional
    public Map<String, Object> deleteItem(Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new DataNotFoundException("Item with ID "+itemId + " Not Found"));
        itemRepository.delete(item);
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("deletedItemId", itemId);
        responseData.put("info", "The Item was removed from the database.");
        return responseData;
    }

    private ItemRes mapToItemRes(Item item) {
        ItemRes itemRes = new ItemRes();
        itemRes.setItemId(item.getItemId());
        itemRes.setItemName(item.getItemName());
        itemRes.setItemPrice(item.getItemPrice());
        itemRes.setItemStock(item.getItemStock());
        return itemRes;
    }
}
