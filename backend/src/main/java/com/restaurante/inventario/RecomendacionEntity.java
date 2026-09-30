package com.restaurante.inventario;
import jakarta.persistence.*;

@Entity
@Table(name = "recomendaciones")
public class RecomendacionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String producto;
    private Integer stockActual;
    private String accionRecomendada; // "COMPRA" o "TRANSFERENCIA"
    private Integer cantidadSugerida;
    private String estado; // "PENDIENTE", "APROBADA"

    public RecomendacionEntity() {}
    public RecomendacionEntity(String producto, Integer stockActual, String accionRecomendada, Integer cantidadSugerida) {
        this.producto = producto;
        this.stockActual = stockActual;
        this.accionRecomendada = accionRecomendada;
        this.cantidadSugerida = cantidadSugerida;
        this.estado = "PENDIENTE";
    }

    public Long getId() { return id; }
    public String getProducto() { return producto; }
    public Integer getStockActual() { return stockActual; }
    public String getAccionRecomendada() { return accionRecomendada; }
    public Integer getCantidadSugerida() { return cantidadSugerida; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}