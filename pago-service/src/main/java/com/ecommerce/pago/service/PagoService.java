package com.ecommerce.pago.service;

import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.ecommerce.pago.event.PagoProcesadoEvent;
import com.ecommerce.pago.event.StockValidadoEvent;
import com.ecommerce.pago.producer.PagoProducer;

@Service
public class PagoService {

    private static final Logger log = LoggerFactory.getLogger(PagoService.class);

    private final PagoProducer producer;

    private final Random random =
            new Random();

    public PagoService(
            PagoProducer producer) {

        this.producer = producer;
    }

    public void procesar(
            StockValidadoEvent event)
            throws Exception {

        log.info("Procesando pago para Pedido ID: {}. Stock disponible: {}", event.pedidoId(), event.stockDisponible());

        if(!event.stockDisponible()) {
            log.warn("Pago rechazado automáticamente para Pedido ID: {} por falta de stock", event.pedidoId());
            producer.publicar(
                    new PagoProcesadoEvent(
                            event.pedidoId(),
                            false,
                            "Sin stock"
                    )
            );

            return;
        }

        boolean aprobado =
                random.nextInt(100) < 70;

        String mensaje = aprobado ? "Pago aprobado" : "Tarjeta rechazada";
        log.info("Pago para Pedido ID: {} procesado. Aprobado: {}. Mensaje: {}", event.pedidoId(), aprobado, mensaje);

        producer.publicar(
                new PagoProcesadoEvent(
                        event.pedidoId(),
                        aprobado,
                        mensaje
                )
        );
    }
}