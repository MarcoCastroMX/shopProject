package com.marco.shopProject.venta.dto;

import com.marco.shopProject.detalleVenta.dto.CrearDetalleVentaDTO;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.List;

@Builder
public record CrearVentaDTO(

        String estado,
        @NotNull(message = "Debe incluir la surcursal")
        Integer sucursalId,
        @NotEmpty(message = "Debe incluir al menos un producto")
        List<CrearDetalleVentaDTO> detalle
) {
}
