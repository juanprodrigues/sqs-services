package com.ecommerce.stock.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.stock.entity.Producto;
import com.ecommerce.stock.event.PedidoCreadoEvent;
import com.ecommerce.stock.event.StockValidadoEvent;
import com.ecommerce.stock.producer.StockProducer;
import com.ecommerce.stock.repository.ProductoRepository;

@Service
@Transactional
public class StockService {

    private static final Logger log = LoggerFactory.getLogger(StockService.class);

    private final ProductoRepository repository;
    private final StockProducer producer;

    public StockService(
            ProductoRepository repository,
            StockProducer producer) {

        this.repository = repository;
        this.producer = producer;
    }

    public void procesar(
            PedidoCreadoEvent event)
            throws Exception {

        log.info("Procesando stock para Pedido ID: {}, Producto: {}, Cantidad: {}", event.pedidoId(), event.producto(), event.cantidad());

        Producto producto =
                repository.findByNombre(
                                event.producto())
                        .orElseThrow();

        boolean disponible =
                producto.getStock()
                        >= event.cantidad();

        if(disponible){

            producto.setStock(
                    producto.getStock()
                            - event.cantidad());

            repository.save(producto);
            log.info("Stock descontado para Pedido ID: {}. Nuevo stock para {}: {}", event.pedidoId(), producto.getNombre(), producto.getStock());
        } else {
            log.warn("No hay suficiente stock para Pedido ID: {}. Solicitado: {}, Disponible: {}", event.pedidoId(), event.cantidad(), producto.getStock());
        }

        log.info("Resultado de validación de stock para Pedido ID: {}. Disponible: {}", event.pedidoId(), disponible);

        producer.publicar(
                new StockValidadoEvent(
                        event.pedidoId(),
                        event.producto(),
                        disponible
                )
        );
    }
}