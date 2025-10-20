package com.blackcode.management_stock.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ItemDto {

    private Long itemId;

    private String itemName;

    private BigDecimal itemPrice;
}
