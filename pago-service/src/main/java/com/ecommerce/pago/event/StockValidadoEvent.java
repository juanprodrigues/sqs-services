package com.ecommerce.pago.event;

public record StockValidadoEvent(
        Long pedidoId,
        String producto,
        boolean stockDisponible
) {
}