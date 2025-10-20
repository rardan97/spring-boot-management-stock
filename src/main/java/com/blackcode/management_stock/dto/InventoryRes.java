package com.blackcode.management_stock.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class InventoryRes {

    private Long inventoryId;

    private ItemRes item;

    private int inventoryQty;

    private String inventoryType;

}
