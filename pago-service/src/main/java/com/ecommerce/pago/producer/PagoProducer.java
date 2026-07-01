package com.ecommerce.pago.producer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.ecommerce.pago.event.PagoProcesadoEvent;

import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class PagoProducer {

    private static final Logger log = LoggerFactory.getLogger(PagoProducer.class);

    private final SqsClient sqsClient;
    private final ObjectMapper mapper;

    private final String queueUrl =
            "http://floci:4566/000000000000/pago-procesado";

    public PagoProducer(
            SqsClient sqsClient,
            ObjectMapper mapper) {

        this.sqsClient = sqsClient;
        this.mapper = mapper;
    }

    public void publicar(
            PagoProcesadoEvent event)
            throws Exception {

        log.info("Publicando evento PagoProcesadoEvent para pedido ID: {} a la cola: {}. Aprobado: {}", event.pedidoId(), queueUrl, event.aprobado());

        try {
            sqsClient.sendMessage(
                    SendMessageRequest.builder()
                            .queueUrl(queueUrl)
                            .messageBody(
                                    mapper.writeValueAsString(event))
                            .build());
        } catch (Exception e) {
            log.error("Error al publicar PagoProcesadoEvent para pedido ID: {}", event.pedidoId(), e);
            throw e;
        }
    }
}