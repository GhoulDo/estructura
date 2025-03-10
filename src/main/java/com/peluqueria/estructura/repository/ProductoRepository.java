package com.peluqueria.estructura.repository;


import com.peluqueria.estructura.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
}

