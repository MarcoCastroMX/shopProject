package com.marco.shopProject.venta.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.marco.shopProject.detalleVenta.entity.DetalleVenta;
import com.marco.shopProject.enums.EstadoEnum;
import com.marco.shopProject.sucursal.entity.Sucursal;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "venta")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Venta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDateTime fecha;

    @Enumerated(EnumType.STRING)
    private EstadoEnum estado;

    private Double total;

    @ManyToOne(cascade = {CascadeType.DETACH,CascadeType.MERGE,CascadeType.PERSIST,CascadeType.REFRESH})
    @JoinColumn(name = "sucursal_id")
    private Sucursal sucursal;

    @OneToMany(mappedBy = "venta", fetch = FetchType.EAGER, cascade = {CascadeType.DETACH,CascadeType.MERGE,CascadeType.PERSIST,CascadeType.REFRESH})
    @JsonManagedReference
    private List<DetalleVenta> detalleVentas;

    @PrePersist
    public void prePersist(){
        this.fecha = LocalDateTime.now();
    }
}
