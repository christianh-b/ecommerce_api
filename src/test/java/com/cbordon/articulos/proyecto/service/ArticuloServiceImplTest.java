package com.cbordon.articulos.proyecto.service;

import com.cbordon.articulos.proyecto.exception.ResourceNotFoundException;
import com.cbordon.articulos.proyecto.model.Articulo;
import com.cbordon.articulos.proyecto.repository.ArticuloRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ArticuloServiceImplTest {

    @Mock
    ArticuloRepository articuloRepository;

    @InjectMocks
    ArticuloServiceImpl articuloService;

    private static final Articulo ARTICULO = new Articulo(1L, "Laptop", 1500.00, null);

    // ── buscarPorNombre ──────────────────────────────────────────────────────

    @Test
    @DisplayName("buscarPorNombre: término con coincidencias → retorna artículos que contienen el término")
    void buscarPorNombre_conCoincidencias_retornaLista() {
        when(articuloRepository.findByNombreContainingIgnoreCase("lap")).thenReturn(List.of(ARTICULO));

        List<Articulo> result = articuloService.buscarPorNombre("lap");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getNombre()).isEqualTo("Laptop");
        verify(articuloRepository).findByNombreContainingIgnoreCase("lap");
    }

    @Test
    @DisplayName("buscarPorNombre: sin coincidencias → retorna lista vacía")
    void buscarPorNombre_sinCoincidencias_retornaListaVacia() {
        when(articuloRepository.findByNombreContainingIgnoreCase("xyz")).thenReturn(Collections.emptyList());

        List<Articulo> result = articuloService.buscarPorNombre("xyz");

        assertThat(result).isEmpty();
        verify(articuloRepository).findByNombreContainingIgnoreCase("xyz");
    }

    // ── listarArticulos ──────────────────────────────────────────────────────

    @Test
    @DisplayName("listarArticulos: repositorio con datos → retorna lista completa")
    void listarArticulos_conDatos_retornaLista() {
        when(articuloRepository.findAll()).thenReturn(List.of(ARTICULO));

        List<Articulo> result = articuloService.listarArticulos();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getNombre()).isEqualTo("Laptop");
        verify(articuloRepository).findAll();
    }

    @Test
    @DisplayName("listarArticulos: repositorio vacío → retorna lista vacía")
    void listarArticulos_sinDatos_retornaListaVacia() {
        when(articuloRepository.findAll()).thenReturn(Collections.emptyList());

        List<Articulo> result = articuloService.listarArticulos();

        assertThat(result).isEmpty();
    }

    // ── buscarArticuloPorId ──────────────────────────────────────────────────

    @Test
    @DisplayName("buscarArticuloPorId: id existente → retorna el artículo")
    void buscarArticuloPorId_existente_retornaArticulo() {
        when(articuloRepository.findById(1L)).thenReturn(Optional.of(ARTICULO));

        Articulo result = articuloService.buscarArticuloPorId(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getNombre()).isEqualTo("Laptop");
        assertThat(result.getPrecio()).isEqualTo(1500.00);
    }

    @Test
    @DisplayName("buscarArticuloPorId: id no existe → lanza ResourceNotFoundException con el id en el mensaje")
    void buscarArticuloPorId_noExiste_lanzaResourceNotFoundException() {
        when(articuloRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> articuloService.buscarArticuloPorId(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    // ── guardarArticulo ──────────────────────────────────────────────────────

    @Test
    @DisplayName("guardarArticulo: datos válidos → guarda en repositorio y retorna el artículo")
    void guardarArticulo_conDatosValidos_guardaYRetorna() {
        when(articuloRepository.save(any(Articulo.class))).thenReturn(ARTICULO);

        Articulo result = articuloService.guardarArticulo(ARTICULO);

        assertThat(result).isEqualTo(ARTICULO);
        verify(articuloRepository).save(ARTICULO);
    }

    // ── actualizarArticulo ───────────────────────────────────────────────────

    @Test
    @DisplayName("actualizarArticulo: id existente → asigna el id al artículo y guarda")
    void actualizarArticulo_existente_asignaIdYGuarda() {
        Articulo articuloActualizado = new Articulo(null, "Laptop Pro", 2000.00, null);
        when(articuloRepository.findById(1L)).thenReturn(Optional.of(ARTICULO));
        when(articuloRepository.save(any(Articulo.class)))
                .thenReturn(new Articulo(1L, "Laptop Pro", 2000.00, null));

        articuloService.actualizarArticulo(1L, articuloActualizado);

        verify(articuloRepository).save(argThat(a -> a.getId().equals(1L)));
    }

    @Test
    @DisplayName("actualizarArticulo: id no existe → lanza excepción y no llama a save")
    void actualizarArticulo_noExiste_lanzaExcepcion() {
        when(articuloRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> articuloService.actualizarArticulo(99L, ARTICULO))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(articuloRepository, never()).save(any());
    }

    // ── borrarArticulo ───────────────────────────────────────────────────────

    @Test
    @DisplayName("borrarArticulo: id existente → llama a deleteById con el id correcto")
    void borrarArticulo_existente_llamaDeleteById() {
        when(articuloRepository.findById(1L)).thenReturn(Optional.of(ARTICULO));
        doNothing().when(articuloRepository).deleteById(1L);

        articuloService.borrarArticulo(1L);

        verify(articuloRepository).deleteById(1L);
    }

    @Test
    @DisplayName("borrarArticulo: id no existe → lanza excepción y no llama a deleteById")
    void borrarArticulo_noExiste_lanzaExcepcion() {
        when(articuloRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> articuloService.borrarArticulo(99L))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(articuloRepository, never()).deleteById(any());
    }
}
