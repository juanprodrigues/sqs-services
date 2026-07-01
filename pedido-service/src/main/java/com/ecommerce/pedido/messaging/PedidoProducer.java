package com.ecommerce.pedido.messaging;

import org.springframework.stereotype.Service;

import com.ecommerce.pedido.event.PedidoCreadoEvent;

import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
@Service
public class PedidoProducer {

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

        String json =
                mapper.writeValueAsString(event);

        sqsClient.sendMessage(
                SendMessageRequest.builder()
                        .queueUrl(queueUrl)
                        .messageBody(json)
                        .build());
    }
}