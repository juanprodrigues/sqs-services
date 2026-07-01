package com.ecommerce.factura.event;

public record PagoProcesadoEvent(
        Long pedidoId,
        boolean aprobado,
        String mensaje
) {
}