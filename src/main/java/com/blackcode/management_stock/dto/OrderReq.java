package com.blackcode.management_stock.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class OrderReq {

    @NotNull(message = "Item ID tidak boleh kosong")
    private Long itemId;

    @Min(value = 1, message = "Quantity harus lebih dari 0")
    private int orderQty;

    @NotNull(message = "Price tidak boleh kosong")
    private BigDecimal price;

}
