package com.blackcode.management_stock.service;

import com.blackcode.management_stock.dto.ItemReq;
import com.blackcode.management_stock.dto.ItemRes;
import com.blackcode.management_stock.exception.DataNotFoundException;
import com.blackcode.management_stock.model.Item;
import com.blackcode.management_stock.repository.ItemRepository;
import com.blackcode.management_stock.service.impl.ItemServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ItemServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private ItemServiceImpl itemService;

    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    void getAllItems_shouldReturnPagedItems() {
        // Arrange
        Item item1 = new Item(1L, "Item A", new BigDecimal("10000"), 10);
        Item item2 = new Item(2L, "Item B", new BigDecimal("20000"), 5);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Item> itemPage = new PageImpl<>(List.of(item1, item2));

        when(itemRepository.findAll(pageable)).thenReturn(itemPage);

        Page<ItemRes> result = itemService.getAllItems(0, 10);

        assertNotNull(result);
        assertEquals(2, result.getTotalElements());

        assertEquals(item1.getItemId(), result.getContent().get(0).getItemId());
        assertEquals(item2.getItemId(), result.getContent().get(1).getItemId());

        verify(itemRepository).findAll(pageable);
    }

    @Test
    void getAllItems_whenEmpty_shouldReturnEmptyPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Item> emptyPage = new PageImpl<>(Collections.emptyList());

        when(itemRepository.findAll(pageable)).thenReturn(emptyPage);

        Page<ItemRes> result = itemService.getAllItems(0, 10);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(itemRepository).findAll(pageable);
    }

    @Test
    void getItemById_whenExists_shouldReturnItemRes() {
        Item item = new Item(1L, "Item A", new BigDecimal("10000"), 10);
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        ItemRes result = itemService.getItemById(1L);

        assertNotNull(result);
        assertEquals(item.getItemId(), result.getItemId());
        assertEquals(item.getItemName(), result.getItemName());
        verify(itemRepository).findById(1L);
    }

    @Test
    void getItemById_whenNotFound_shouldThrowException() {
        when(itemRepository.findById(999L)).thenReturn(Optional.empty());

        DataNotFoundException ex = assertThrows(DataNotFoundException.class, () -> {
            itemService.getItemById(999L);
        });

        assertEquals("Item not found with id: 999", ex.getMessage());
        verify(itemRepository).findById(999L);
    }

    @Test
    void createItem_shouldSaveAndReturnItemRes() {
        ItemReq itemReq = new ItemReq("Item A", new BigDecimal("10000"), 10);
        Item savedItem = new Item(1L, "Item A", new BigDecimal("10000"), 10);

        when(itemRepository.save(any(Item.class))).thenReturn(savedItem);

        ItemRes result = itemService.createItem(itemReq);

        assertNotNull(result);
        assertEquals(savedItem.getItemId(), result.getItemId());

        ArgumentCaptor<Item> itemCaptor = ArgumentCaptor.forClass(Item.class);
        verify(itemRepository).save(itemCaptor.capture());

        Item captured = itemCaptor.getValue();
        assertEquals(itemReq.getItemName(), captured.getItemName());
        assertEquals(itemReq.getItemPrice(), captured.getItemPrice());
        assertEquals(itemReq.getItemStock(), captured.getItemStock());
    }

    @Test
    void updateItem_whenExists_shouldUpdateAndReturnItemRes() {
        ItemReq itemReq = new ItemReq("Updated Item", new BigDecimal("15000"), 7);
        Item existingItem = new Item(1L, "Old Item", new BigDecimal("10000"), 10);

        when(itemRepository.findById(1L)).thenReturn(Optional.of(existingItem));
        when(itemRepository.save(any(Item.class))).thenReturn(existingItem);

        ItemRes result = itemService.updateItem(1L, itemReq);

        assertNotNull(result);
        assertEquals(itemReq.getItemName(), result.getItemName());
        verify(itemRepository).findById(1L);
        verify(itemRepository).save(existingItem);
    }

    @Test
    void updateItem_whenNotFound_shouldThrowException() {
        ItemReq itemReq = new ItemReq("Item A", new BigDecimal("10000"), 10);
        when(itemRepository.findById(999L)).thenReturn(Optional.empty());

        DataNotFoundException ex = assertThrows(DataNotFoundException.class, () -> {
            itemService.updateItem(999L, itemReq);
        });

        assertEquals("Item with ID 999 Not Found", ex.getMessage());
        verify(itemRepository).findById(999L);
        verify(itemRepository, never()).save(any());
    }

    @Test
    void deleteItem_whenExists_shouldDeleteSuccessfully() {
        Item item = new Item(1L, "Item A", new BigDecimal("10000"), 10);
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        Map<String, Object> result = itemService.deleteItem(1L);

        assertEquals(1L, result.get("deletedItemId"));
        assertEquals("The Item was removed from the database.", result.get("info"));

        verify(itemRepository).delete(item);
    }

    @Test
    void deleteItem_whenNotFound_shouldThrowException() {
        when(itemRepository.findById(404L)).thenReturn(Optional.empty());

        DataNotFoundException ex = assertThrows(DataNotFoundException.class, () -> {
            itemService.deleteItem(404L);
        });

        assertEquals("Item with ID 404 Not Found", ex.getMessage());
        verify(itemRepository, never()).deleteById(anyLong());
    }
}