package com.ecommerce.pedido.service;

import com.ecommerce.pedido.dto.CrearPedidoRequest;
import com.ecommerce.pedido.entity.Pedido;
import com.ecommerce.pedido.event.PedidoCreadoEvent;
import com.ecommerce.pedido.messaging.PedidoProducer;
import com.ecommerce.pedido.repository.PedidoRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PedidoService {

    private static final Logger log = LoggerFactory.getLogger(PedidoService.class);

    private final PedidoRepository repository;
    private final PedidoProducer producer;

    public PedidoService(
            PedidoRepository repository,
            PedidoProducer producer) {

        this.repository = repository;
        this.producer = producer;
    }

    @Transactional
    public Pedido crear(CrearPedidoRequest dto)
            throws Exception {

        log.info("Creando pedido para cliente: {}, producto: {}, cantidad: {}", dto.cliente(), dto.producto(), dto.cantidad());

        Pedido pedido = new Pedido(
                dto.cliente(),
                dto.producto(),
                dto.cantidad(),
                "CREADO"
        );

        Pedido guardado =
                repository.save(pedido);

        log.info("Pedido guardado con ID: {}", guardado.getId());

        producer.publicar(
                new PedidoCreadoEvent(
                        guardado.getId(),
                        guardado.getCliente(),
                        guardado.getProducto(),
                        guardado.getCantidad()
                )
        );

        return guardado;
    }
}