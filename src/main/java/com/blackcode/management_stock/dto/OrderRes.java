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
public class OrderRes {

    private String orderNo;

    private ItemDto item;

    private int orderQty;

    private BigDecimal price;

}
