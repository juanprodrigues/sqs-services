package com.ecommerce.pago.event;

public record PagoProcesadoEvent(
        Long pedidoId,
        boolean aprobado,
        String mensaje
) {
}