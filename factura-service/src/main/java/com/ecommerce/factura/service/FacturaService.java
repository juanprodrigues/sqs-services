package com.ecommerce.factura.service;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.ecommerce.factura.event.FacturaGeneradaEvent;
import com.ecommerce.factura.event.PagoProcesadoEvent;
import com.ecommerce.factura.service.producer.FacturaProducer;

@Service
public class FacturaService {

    private static final Logger log = LoggerFactory.getLogger(FacturaService.class);

    private final PdfService pdfService;
    private final S3StorageService storage;
    private final FacturaProducer producer;

    public FacturaService(
            PdfService pdfService,
            S3StorageService storage,
            FacturaProducer producer) {

        this.pdfService = pdfService;
        this.storage = storage;
        this.producer = producer;
    }

    public void procesar(
            PagoProcesadoEvent event)
            throws Exception {

        log.info("Procesando factura para Pedido ID: {}", event.pedidoId());

        if(!event.aprobado()) {
            log.info("Pago para Pedido ID: {} no aprobado. No se genera factura.", event.pedidoId());
            return;
        }

        File pdf =
                pdfService.generarFactura(
                        event.pedidoId());
        log.info("PDF de factura generado localmente para Pedido ID: {}", event.pedidoId());

        String key =
                storage.subirFactura(pdf);
        log.info("Factura subida exitosamente a S3 con key: {} para Pedido ID: {}", key, event.pedidoId());

        producer.publicar(
                new FacturaGeneradaEvent(
                        event.pedidoId(),
                        key));
    }
}
