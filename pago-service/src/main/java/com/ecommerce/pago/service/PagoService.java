package com.ecommerce.pago.service;

import java.util.Random;

import org.springframework.stereotype.Service;

import com.ecommerce.pago.event.PagoProcesadoEvent;
import com.ecommerce.pago.event.StockValidadoEvent;
import com.ecommerce.pago.producer.PagoProducer;

@Service
public class PagoService {

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

        if(!event.stockDisponible()) {

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

        producer.publicar(
                new PagoProcesadoEvent(
                        event.pedidoId(),
                        aprobado,
                        aprobado
                                ? "Pago aprobado"
                                : "Tarjeta rechazada"
                )
        );
    }
}