package com.ecommerce.pedido.dto;

public record CrearPedidoRequest(
        String cliente,
        String producto,
        Integer cantidad
) {
}