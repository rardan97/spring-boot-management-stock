package com.blackcode.management_stock.dto;

import com.blackcode.management_stock.model.InventoryType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class InventoryReq {

    @NotNull(message = "Item ID harus diisi")
    private Long itemId;

    @NotNull(message = "Jumlah inventory harus diisi")
    @Min(value = 1, message = "Jumlah inventory harus minimal 1")
    private Integer inventoryQty;

    @NotNull(message = "Tipe inventory harus diisi")
    private InventoryType inventoryType;

}
