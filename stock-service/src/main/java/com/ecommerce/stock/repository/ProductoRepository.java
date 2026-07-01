package com.ecommerce.stock.repository;

import com.ecommerce.stock.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductoRepository
        extends JpaRepository<Producto, Long> {

    Optional<Producto> findByNombre(String nombre);
}