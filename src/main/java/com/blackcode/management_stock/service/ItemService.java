package com.blackcode.management_stock.service;

import com.blackcode.management_stock.dto.ItemReq;
import com.blackcode.management_stock.dto.ItemRes;
import org.springframework.data.domain.Page;

import java.util.Map;

public interface ItemService {

    Page<ItemRes> getAllItems(int page, int size);

    ItemRes getItemById(Long itemId);

    ItemRes createItem(ItemReq itemReq);

    ItemRes updateItem(Long itemId, ItemReq itemReq);

    Map<String, Object> deleteItem(Long itemId);
}
