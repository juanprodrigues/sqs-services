package com.ecommerce.factura.service.producer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.ecommerce.factura.event.FacturaGeneradaEvent;
import com.fasterxml.jackson.databind.ObjectMapper;

import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

@Service
public class FacturaProducer {

    private static final Logger log = LoggerFactory.getLogger(FacturaProducer.class);

    private final SqsClient sqsClient;
    private final ObjectMapper mapper;

    private final String queueUrl =
            "http://floci:4566/000000000000/factura-generada";

    public FacturaProducer(
            SqsClient sqsClient,
            ObjectMapper mapper) {

        this.sqsClient = sqsClient;
        this.mapper = mapper;
    }

    public void publicar(
            FacturaGeneradaEvent event)
            throws Exception {

        log.info("Publicando evento FacturaGeneradaEvent para pedido ID: {} a la cola: {}", event.pedidoId(), queueUrl);

        try {
            sqsClient.sendMessage(
                    SendMessageRequest.builder()
                            .queueUrl(queueUrl)
                            .messageBody(
                                    mapper.writeValueAsString(event))
                            .build());
        } catch (Exception e) {
            log.error("Error al publicar FacturaGeneradaEvent para pedido ID: {}", event.pedidoId(), e);
            throw e;
        }
    }
}