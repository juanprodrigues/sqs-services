package com.ecommerce.stock.event;

public record PedidoCreadoEvent(
        Long pedidoId,
        String cliente,
        String producto,
        Integer cantidad
) {
}