# ejercicio1-inventario-restaurantes
# Entregable: Ejercicio 1 - Inventario y Riesgo de Quiebre

## 1. Objetivo, Actores y Alcance
*   **Objetivo:** Proporcionar visibilidad temprana sobre el riesgo de quiebre de stock en las bodegas de la cadena de restaurantes, generando recomendaciones accionables que requieran aprobación humana antes de integrarse asíncronamente con el sistema de compras.
*   **Actores:**
    *   **Encargado de Bodega:** Revisa y aprueba o rechaza las recomendaciones.
    *   **Sistema de Compras (Externo):** Recibe las órdenes aprobadas.
    *   **Motor de Pronóstico (Interno):** Evalúa el consumo histórico y calcula el riesgo de quiebre.
*   **Alcance:** Estimación del riesgo, generación de la recomendación, interfaz de aprobación e integración mediante eventos.

## 2. Requisitos Funcionales y de Calidad
*   **Funcionales:**
    *   Consultar existencias y estimar riesgo (simulado en la prueba de concepto).
    *   Recomendar transferencias entre bodegas o compras urgentes.
    *   Requerir aprobación humana antes de ejecutar la acción.
    *   Registrar auditoría de la decisión.
*   **Calidad (Atributos):**
    *   **Desacoplamiento:** La caída del sistema de compras no debe impedir que el encargado apruebe recomendaciones.
    *   **Tolerancia a fallos:** Si el pronóstico falla, el sistema debe permitir compras manuales basadas en el inventario actual.

## 3. Diagramas C4

**Diagrama de Contexto:**
```mermaid
C4Context
    Person(encargado, "Encargado de Bodega")
    System(sistemaInv, "Sistema de Inventario y Riesgos", "Calcula quiebres y sugiere acciones.")
    System_Ext(compras, "Sistema de Compras ERP", "Ejecuta compras o transferencias.")

    Rel(encargado, sistemaInv, "Revisa y aprueba recomendaciones")
    Rel(sistemaInv, compras, "Envía orden de ejecución (asíncrono)")

    C4Container
    System_Boundary(monolito, "Sistema de Inventario (Monolito Modular)") {
        Container(api, "API (Spring Boot)", "Java", "Contiene módulos lógicos: Inventario, Pronóstico, Aprobación.")
        ContainerDb(db, "Base de Datos", "PostgreSQL", "Guarda niveles de stock e historial de aprobaciones.")
        ContainerQueue(cola, "Message Broker", "RabbitMQ", "Encola las acciones aprobadas.")
    }
    System_Ext(compras, "API de Compras (Legacy)")

    Rel(api, db, "Lee stock / Guarda auditoría")
    Rel(api, cola, "Publica evento: 'RecomendacionAprobada'")
    Rel(cola, compras, "Sistema externo consume el evento")


    4. Flujo de una Operación Crítica (Aprobación)
El Motor de Pronóstico (módulo lógico) detecta que las Papas Fritas están en riesgo. Crea un registro PENDIENTE en la Base de Datos.

El encargado abre la aplicación y visualiza las alertas.

El encargado pulsa Aprobar.

La API cambia el estado a APROBADA y guarda el log de auditoría.

La API publica un evento en RabbitMQ (COMPRA_SOLICITADA).

La API responde exitosamente al usuario web al instante.

El sistema de compras existente, a su propio ritmo, lee el mensaje de RabbitMQ y emite la orden de compra real al proveedor.

5. Stack Propuesto y Justificación
Arquitectura: Monolito Modular con Spring Boot. ¿Por qué? Porque el dominio del negocio (restaurantes) está muy cohesionado. Dividir inventario y pronóstico en microservicios separados por red añadiría latencia y complejidad innecesaria para esta fase del proyecto. El desacoplamiento lógico con paquetes Java es suficiente.

Base de datos: PostgreSQL. Ideal para mantener integridad relacional entre Bodegas, Productos y Auditorías.

Integración (RabbitMQ): La recomendación se publica como evento. Esto responde directamente al requisito del ejercicio: si el sistema de compras está lento, no bloquea al encargado.

(Nota: React fue sugerido en el enunciado, pero por requerimientos específicos de despliegue consolidado se implementó HTML/JS vanilla integrado en Spring Boot).

6. Decisiones Arquitectónicas (ADRs)
ADR 1 (Arquitectónica): Integración basada en eventos vs API REST.

Decisión: Comunicar las aprobaciones al sistema de compras vía RabbitMQ (Event-Driven) en lugar de consultar la API del sistema de compras directamente.

Justificación: El sistema de compras existente suele ser un ERP (ej. SAP) lento. Emitir un evento asíncrono protege el rendimiento de nuestra nueva aplicación.

ADR 2 (Técnica): Manejo de caída del servicio de pronóstico.

Decisión (Respondiendo a la pregunta del ejercicio): Si el motor de pronóstico falla, el módulo de Aprobación seguirá funcionando con los datos de inventario en crudo (Fall-back). Los usuarios podrán crear recomendaciones manualmente sin depender de la IA/algoritmo.

7. Riesgos y Mitigaciones
Riesgo: El encargado aprueba por error una orden de compra masiva.

Mitigación: Implementar límites máximos de presupuesto por rol. Las compras mayores a $1,000 requerirán una segunda firma digital.

Riesgo: Pérdida de mensajes entre nuestro sistema y el de compras.

Mitigación: Usar colas persistentes en RabbitMQ y un patrón "Outbox" en PostgreSQL para reconciliación nocturna.

Riesgo: El motor de pronóstico se descalibra y lanza falsas alarmas (ej. recomienda comprar mucho porque hubo un evento inusual el mes pasado).

Mitigación: Mantener siempre el "human-in-the-loop" (aprobación humana) que es el núcleo de este diseño, evitando compras 100% automatizadas.

8. Métricas Clave
Negocio: Disminución en el % de quiebres de stock por producto.

Negocio/Operación: Tiempo medio de aprobación (desde que el sistema alerta hasta que el humano hace clic en aprobar).

Técnica: Recomendaciones Aceptadas vs Rechazadas (para medir la precisión del algoritmo de pronóstico).