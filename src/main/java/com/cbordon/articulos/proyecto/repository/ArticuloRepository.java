package com.cbordon.articulos.proyecto.repository;

import com.cbordon.articulos.proyecto.model.Articulo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArticuloRepository extends JpaRepository<Articulo, Long> {
    List<Articulo> findByNombreContainingIgnoreCase(String nombre);
}
