package com.blackcode.management_stock.service;

import com.blackcode.management_stock.dto.InventoryReq;
import com.blackcode.management_stock.dto.InventoryRes;
import com.blackcode.management_stock.exception.DataNotFoundException;
import com.blackcode.management_stock.exception.InvalidStockException;
import com.blackcode.management_stock.exception.NotEnoughStockException;
import com.blackcode.management_stock.model.Inventory;
import com.blackcode.management_stock.model.InventoryType;
import com.blackcode.management_stock.model.Item;
import com.blackcode.management_stock.repository.InventoryRepository;
import com.blackcode.management_stock.repository.ItemRepository;
import com.blackcode.management_stock.service.impl.InventoryServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class InventoryServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private InventoryServiceImpl inventoryService;

    private AutoCloseable closeable;

    @BeforeEach
    void setup() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    void createInventory_transfer_shouldAddStock() {
        InventoryReq req = new InventoryReq(1L, 10, InventoryType.T);
        Item item = new Item(1L, "Item A", null, 5);

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(i -> {
            Inventory inv = (Inventory) i.getArguments()[0];
            inv.setInventoryId(100L);
            return inv;
        });

        InventoryRes res = inventoryService.createInventory(req);

        assertEquals(15, item.getItemStock());
        assertEquals("T", res.getInventoryType());
        verify(itemRepository).save(item);
        verify(inventoryRepository).save(any(Inventory.class));
    }

    @Test
    void createInventory_withdrawal_shouldSubtractStock() {
        InventoryReq req = new InventoryReq(1L, 3, InventoryType.W);
        Item item = new Item(1L, "Item A", null, 10);

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(i -> {
            Inventory inv = (Inventory) i.getArguments()[0];
            inv.setInventoryId(101L);
            return inv;
        });

        InventoryRes res = inventoryService.createInventory(req);

        assertEquals(7, item.getItemStock());
        assertEquals("W", res.getInventoryType());
        verify(itemRepository).save(item);
    }

    @Test
    void createInventory_withdrawal_shouldFail_ifStockNotEnough() {
        InventoryReq req = new InventoryReq(1L, 15, InventoryType.W);
        Item item = new Item(1L, "Item A", null, 10);

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        assertThrows(NotEnoughStockException.class, () -> inventoryService.createInventory(req));
        verify(itemRepository, never()).save(any());
    }

    @Test
    void updateInventory_shouldUpdateAndAdjustStock() {
        Item item = new Item(1L, "Item A", null, 10);
        Inventory inventory = new Inventory(100L, item, 5, InventoryType.T);
        InventoryReq req = new InventoryReq(1L, 2, InventoryType.W);

        when(inventoryRepository.findById(100L)).thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(i -> i.getArguments()[0]);

        InventoryRes res = inventoryService.updateInventory(100L, req);
        assertEquals(3, item.getItemStock());
        assertEquals("W", res.getInventoryType());
        verify(itemRepository).save(item);
    }

    @Test
    void updateInventory_shouldThrow_ifStockBecomesNegative_dueToWithdrawal() {
        Item item = new Item(1L, "Item A", null, 3);
        Inventory inventory = new Inventory(100L, item, 5, InventoryType.T);
        InventoryReq req = new InventoryReq(1L, 10, InventoryType.W);

        when(inventoryRepository.findById(100L)).thenReturn(Optional.of(inventory));

        assertThrows(NotEnoughStockException.class, () -> inventoryService.updateInventory(100L, req));
    }

    @Test
    void updateInventory_shouldThrow_InvalidStockException_afterUpdateStockBecomesNegative() {
        Item item = new Item(1L, "Item A", null, 2);
        Inventory inventory = new Inventory(100L, item, 5, InventoryType.T);

        InventoryReq req = new InventoryReq(1L, 2, InventoryType.T);

        when(inventoryRepository.findById(100L)).thenReturn(Optional.of(inventory));

        assertThrows(InvalidStockException.class, () -> inventoryService.updateInventory(100L, req));
    }

    @Test
    void getInventoryById_shouldReturnInventory() {
        Inventory inventory = new Inventory(100L, new Item(), 5, InventoryType.T);

        when(inventoryRepository.findById(100L)).thenReturn(Optional.of(inventory));

        InventoryRes res = inventoryService.getInventoryById(100L);

        assertEquals(5, res.getInventoryQty());
        assertEquals("T", res.getInventoryType());
    }

    @Test
    void getAllInventory_shouldReturnPagedData() {
        Pageable pageable = PageRequest.of(0, 10);
        List<Inventory> inventories = List.of(new Inventory(100L, new Item(), 3, InventoryType.T));
        Page<Inventory> page = new PageImpl<>(inventories, pageable, inventories.size());

        when(inventoryRepository.findAll(pageable)).thenReturn(page);

        Page<InventoryRes> result = inventoryService.getAllInventory(0, 10);

        assertEquals(1, result.getTotalElements());
        assertEquals("T", result.getContent().get(0).getInventoryType());
    }

    @Test
    void deleteInventory_shouldDeleteSuccessfully() {
        Inventory inventory = new Inventory(123L, new Item(), 2, InventoryType.T);
        when(inventoryRepository.findById(123L)).thenReturn(Optional.of(inventory));

        Map<String, Object> response = inventoryService.deleteInventory(123L);

        verify(inventoryRepository).delete(inventory);
        assertEquals(123L, response.get("deletedInventoryId"));
    }

    @Test
    void deleteInventory_shouldThrow_ifNotFound() {
        when(inventoryRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(DataNotFoundException.class, () -> inventoryService.deleteInventory(999L));
    }

}
