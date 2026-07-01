package com.ecommerce.pedido.service;

import com.ecommerce.pedido.dto.CrearPedidoRequest;
import com.ecommerce.pedido.entity.Pedido;
import com.ecommerce.pedido.event.PedidoCreadoEvent;
import com.ecommerce.pedido.messaging.PedidoProducer;
import com.ecommerce.pedido.repository.PedidoRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PedidoService {

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

        Pedido pedido = new Pedido(
                dto.cliente(),
                dto.producto(),
                dto.cantidad(),
                "CREADO"
        );

        Pedido guardado =
                repository.save(pedido);

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