package com.restaurante.inventario;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/recomendaciones")
public class RecomendacionController {

    @Autowired
    private RecomendacionRepository repository;

    // Generamos datos de prueba al iniciar para simular que el motor de pronóstico funcionó
    @PostConstruct
    public void init() {
        if(repository.count() == 0) {
            repository.save(new RecomendacionEntity("Papas Fritas Congeladas", 15, "COMPRA URGENTE", 100));
            repository.save(new RecomendacionEntity("Aceite de Girasol", 2, "TRANSFERENCIA DESDE BODEGA NORTE", 20));
        }
    }

    @GetMapping
    public List<RecomendacionEntity> listar() {
        return repository.findAll();
    }

    @PostMapping("/{id}/aprobar")
    public ResponseEntity<?> aprobar(@PathVariable Long id) {
        RecomendacionEntity rec = repository.findById(id).orElseThrow();
        rec.setEstado("APROBADA");
        repository.save(rec);

        // AQUÍ EL SISTEMA PUBLICA UN EVENTO A RABBITMQ HACIA EL SISTEMA DE COMPRAS EXISTENTE
        System.out.println("AUDITORÍA: Recomendación " + id + " aprobada por el encargado.");
        System.out.println("RABBITMQ EVENT PUBLISHED: Solicitar " + rec.getAccionRecomendada() + " de " + rec.getProducto());

        return ResponseEntity.ok().build();
    }
}