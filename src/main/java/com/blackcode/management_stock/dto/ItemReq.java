package com.blackcode.management_stock.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
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
public class ItemReq {

    @NotBlank(message = "Item name harus diisi")
    private String itemName;

    @NotNull(message = "Item price harus diisi")
    @DecimalMin(value = "1", inclusive = true, message = "Harga harus minimal 1")
    private BigDecimal itemPrice;

    @NotNull(message = "Item stock harus diisi")
    @Min(value = 1, message = "Stok tidak boleh kurang dari 1")
    private Integer itemStock;

}
