package com.ecommerce.pedido.event;

public record PedidoCreadoEvent(
        Long pedidoId,
        String cliente,
        String producto,
        Integer cantidad
) {
}