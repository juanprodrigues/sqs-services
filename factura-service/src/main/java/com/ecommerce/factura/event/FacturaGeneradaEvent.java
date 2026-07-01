package com.ecommerce.factura.event;

public record FacturaGeneradaEvent(
        Long pedidoId,
        String archivoPdf
) {
}