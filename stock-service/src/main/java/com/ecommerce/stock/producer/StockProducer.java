package com.ecommerce.stock.producer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.ecommerce.stock.event.StockValidadoEvent;
import com.fasterxml.jackson.databind.ObjectMapper;

import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

@Service
public class StockProducer {

    private static final Logger log = LoggerFactory.getLogger(StockProducer.class);

    private final SqsClient sqsClient;
    private final ObjectMapper mapper;

    private final String queueUrl =
            "http://floci:4566/000000000000/stock-validado";

    public StockProducer(
            SqsClient sqsClient,
            ObjectMapper mapper) {

        this.sqsClient = sqsClient;
        this.mapper = mapper;
    }

    public void publicar(
            StockValidadoEvent event)
            throws Exception {

        log.info("Publicando evento StockValidadoEvent para pedido ID: {} a la cola: {}. Disponible: {}", event.pedidoId(), queueUrl, event.stockDisponible());

        try {
            sqsClient.sendMessage(
                    SendMessageRequest.builder()
                            .queueUrl(queueUrl)
                            .messageBody(
                                    mapper.writeValueAsString(event))
                            .build());
        } catch (Exception e) {
            log.error("Error al publicar StockValidadoEvent para pedido ID: {}", event.pedidoId(), e);
            throw e;
        }
    }
}