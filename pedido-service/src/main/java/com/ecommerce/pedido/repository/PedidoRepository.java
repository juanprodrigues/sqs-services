package com.ecommerce.pedido.repository;

import com.ecommerce.pedido.entity.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PedidoRepository
        extends JpaRepository<Pedido, Long> {
}