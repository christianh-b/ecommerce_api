package com.cbordon.articulos.proyecto.service;

import com.cbordon.articulos.proyecto.exception.ResourceNotFoundException;
import com.cbordon.articulos.proyecto.model.Articulo;
import com.cbordon.articulos.proyecto.repository.ArticuloRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ArticuloServiceImpl implements ArticuloService {

    private final ArticuloRepository articuloRepository;

    @Autowired
    public ArticuloServiceImpl(ArticuloRepository articuloRepository) {
        this.articuloRepository = articuloRepository;
    }

    @Override
    public List<Articulo> listarArticulos() {
        return articuloRepository.findAll();
    }

    @Override
    public List<Articulo> buscarPorNombre(String nombre) {
        return articuloRepository.findByNombreContainingIgnoreCase(nombre);
    }

    @Override
    public Articulo buscarArticuloPorId(Long id) {
        return articuloRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Artículo con id " + id + " no encontrado"));
    }

    @Override
    public Articulo guardarArticulo(Articulo articulo) {
        return articuloRepository.save(articulo);
    }

    @Override
    public Articulo actualizarArticulo(Long id, Articulo articulo) {
        buscarArticuloPorId(id);
        articulo.setId(id);
        return articuloRepository.save(articulo);
    }

    @Override
    public void borrarArticulo(Long id) {
        buscarArticuloPorId(id);
        articuloRepository.deleteById(id);
    }
}
