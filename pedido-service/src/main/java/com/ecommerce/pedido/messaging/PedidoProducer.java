package com.ecommerce.pedido.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.ecommerce.pedido.event.PedidoCreadoEvent;

import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class PedidoProducer {

    private static final Logger log = LoggerFactory.getLogger(PedidoProducer.class);

    private final SqsClient sqsClient;
    private final ObjectMapper mapper;

    private final String queueUrl =
            "http://floci:4566/000000000000/pedido-creado";

    public PedidoProducer(
            SqsClient sqsClient,
            ObjectMapper mapper) {

        this.sqsClient = sqsClient;
        this.mapper = mapper;
    }

    public void publicar(PedidoCreadoEvent event)
            throws Exception {

        log.info("Publicando evento PedidoCreadoEvent para pedido ID: {} a la cola: {}", event.pedidoId(), queueUrl);

        try {
            String json =
                    mapper.writeValueAsString(event);

            sqsClient.sendMessage(
                    SendMessageRequest.builder()
                            .queueUrl(queueUrl)
                            .messageBody(json)
                            .build());
        } catch (Exception e) {
            log.error("Error al publicar PedidoCreadoEvent para pedido ID: {}", event.pedidoId(), e);
            throw e;
        }
    }
}