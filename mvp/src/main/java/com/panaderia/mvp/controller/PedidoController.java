package com.panaderia.mvp.controller;

import com.panaderia.mvp.dto.CrearPedidoRequest;
import com.panaderia.mvp.dto.PedidoResponse;
import com.panaderia.mvp.dto.ValidarFechaRequest;
import com.panaderia.mvp.dto.ValidarFechaResponse;
import com.panaderia.mvp.model.CapacidadCocina;
import com.panaderia.mvp.service.CocinaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pedidos")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PedidoController {

    private final CocinaService cocinaService;

    @PostMapping("/validar-fecha")
    public ResponseEntity<ValidarFechaResponse> validarFecha(@Valid @RequestBody ValidarFechaRequest request) {
        ValidarFechaResponse response = cocinaService.validarDisponibilidad(request.getFechaEntrega());
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<?> crearPedido(@Valid @RequestBody CrearPedidoRequest request) {
        try {
            PedidoResponse pedido = cocinaService.registrarPedido(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(pedido);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "error", e.getMessage(),
                    "disponible", false
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "error", "Ocurrio un error al registrar el pedido: " + e.getMessage()
            ));
        }
    }

    @GetMapping
    public ResponseEntity<List<PedidoResponse>> listarPedidos() {
        return ResponseEntity.ok(cocinaService.listarPedidos());
    }

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<PedidoResponse>> listarPedidosPorUsuario(@PathVariable Long usuarioId) {
        return ResponseEntity.ok(cocinaService.listarPedidosPorUsuario(usuarioId));
    }

    @GetMapping("/capacidad")
    public ResponseEntity<CapacidadCocina> consultarCapacidad(
            @RequestParam("fecha") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return ResponseEntity.ok(cocinaService.obtenerOCrearCapacidad(fecha));
    }
}
