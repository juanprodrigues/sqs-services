package com.ecommerce.factura.service;

import java.io.File;

import org.springframework.stereotype.Service;

import com.ecommerce.factura.event.FacturaGeneradaEvent;
import com.ecommerce.factura.event.PagoProcesadoEvent;
import com.ecommerce.factura.service.producer.FacturaProducer;

@Service
public class FacturaService {

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

        if(!event.aprobado()) {

            return;
        }

        File pdf =
                pdfService.generarFactura(
                        event.pedidoId());

        String key =
                storage.subirFactura(pdf);

        producer.publicar(
                new FacturaGeneradaEvent(
                        event.pedidoId(),
                        key));
    }
}
