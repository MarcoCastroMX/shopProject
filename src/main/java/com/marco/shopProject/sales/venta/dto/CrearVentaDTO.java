package com.marco.shopProject.sales.venta.dto;

import com.marco.shopProject.sales.detalleVenta.dto.CrearDetalleVentaDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.List;

@Builder
public record CrearVentaDTO(
        @NotNull(message = "Debe incluir la surcursal")
        Long sucursalId,

        @NotEmpty(message = "Debe incluir al menos un producto")
        @Valid
        List<CrearDetalleVentaDTO> detalle
) {
}
