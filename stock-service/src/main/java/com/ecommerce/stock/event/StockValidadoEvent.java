package com.ecommerce.stock.event;

public record StockValidadoEvent(
        Long pedidoId,
        String producto,
        boolean stockDisponible
) {
}