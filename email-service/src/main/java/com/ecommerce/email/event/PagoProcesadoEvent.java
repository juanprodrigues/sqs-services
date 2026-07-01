package com.ecommerce.email.event;

public record PagoProcesadoEvent(
        Long pedidoId,
        boolean aprobado,
        String mensaje
) {
}