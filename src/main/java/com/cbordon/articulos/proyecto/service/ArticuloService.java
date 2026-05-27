package com.cbordon.articulos.proyecto.service;

import com.cbordon.articulos.proyecto.model.Articulo;

import java.util.List;

public interface ArticuloService {
    List<Articulo> listarArticulos();
    List<Articulo> buscarPorNombre(String nombre);
    Articulo buscarArticuloPorId(Long id);
    Articulo guardarArticulo(Articulo articulo);
    Articulo actualizarArticulo(Long id, Articulo articulo);
    void borrarArticulo(Long id);
}
