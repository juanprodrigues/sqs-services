package com.ecommerce.stock.service;

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
        }

        producer.publicar(
                new StockValidadoEvent(
                        event.pedidoId(),
                        event.producto(),
                        disponible
                )
        );
    }
}