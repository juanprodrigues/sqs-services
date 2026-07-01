package com.ecommerce.stock.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.ecommerce.stock.event.PedidoCreadoEvent;
import com.ecommerce.stock.service.StockService;
import com.fasterxml.jackson.databind.ObjectMapper;

import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageResponse;

@Component
public class PedidoConsumer {

    private static final Logger log = LoggerFactory.getLogger(PedidoConsumer.class);

    private final SqsClient sqsClient;
    private final StockService stockService;
    private final ObjectMapper mapper;

    private final String queueUrl =
            "http://floci:4566/000000000000/pedido-creado";

    public PedidoConsumer(
            SqsClient sqsClient,
            StockService stockService,
            ObjectMapper mapper) {

        this.sqsClient = sqsClient;
        this.stockService = stockService;
        this.mapper = mapper;
    }

    @Scheduled(fixedDelay = 5000)
    public void escuchar() {

        ReceiveMessageResponse response =
                sqsClient.receiveMessage(
                        ReceiveMessageRequest.builder()
                                .queueUrl(queueUrl)
                                .maxNumberOfMessages(10)
                                .build());

        response.messages()
                .forEach(msg -> {

                    log.info("Mensaje recibido de la cola pedido-creado: {}", msg.body());

                    try {

                        PedidoCreadoEvent event =
                                mapper.readValue(
                                        msg.body(),
                                        PedidoCreadoEvent.class);

                        stockService.procesar(event);

                        sqsClient.deleteMessage(
                                DeleteMessageRequest.builder()
                                        .queueUrl(queueUrl)
                                        .receiptHandle(
                                                msg.receiptHandle())
                                        .build());

                    } catch (Exception e) {
                        log.error("Error al procesar mensaje de pedido-creado", e);
                    }
                });
    }
}