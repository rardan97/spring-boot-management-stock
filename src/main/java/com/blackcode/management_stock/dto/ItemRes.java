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
public class ItemRes {

    private Long itemId;

    private String itemName;

    private BigDecimal itemPrice;

    private Integer itemStock;

}
