package com.panaderia.mvp.service;

import com.panaderia.mvp.dto.CrearPedidoRequest;
import com.panaderia.mvp.dto.PedidoResponse;
import com.panaderia.mvp.dto.ValidarFechaResponse;
import com.panaderia.mvp.model.CapacidadCocina;
import com.panaderia.mvp.model.EstadoPedido;
import com.panaderia.mvp.model.Pedido;
import com.panaderia.mvp.model.Usuario;
import com.panaderia.mvp.repository.CapacidadCocinaRepository;
import com.panaderia.mvp.repository.PedidoRepository;
import com.panaderia.mvp.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CocinaService {

    private final PedidoRepository pedidoRepository;
    private final CapacidadCocinaRepository capacidadCocinaRepository;
    private final UsuarioRepository usuarioRepository;

    private static final int LIMITE_POR_DEFECTO = 5;

    @Transactional
    public ValidarFechaResponse validarDisponibilidad(LocalDate fecha) {
        if (fecha.isBefore(LocalDate.now())) {
            return ValidarFechaResponse.builder()
                    .fecha(fecha)
                    .disponible(false)
                    .cuposRestantes(0)
                    .limiteDiario(LIMITE_POR_DEFECTO)
                    .mensaje("La fecha solicitada ya paso. Selecciona una fecha futura.")
                    .build();
        }

        CapacidadCocina capacidad = obtenerOCrearCapacidad(fecha);
        int disponibles = capacidad.cuposDisponibles();

        boolean disponible = disponibles > 0;
        String mensaje = disponible
                ? "Capacidad disponible en cocina (" + disponibles + " cupos restantes de " + capacidad.getLimitePasteles() + ")."
                : "Cocina saturada para el " + fecha + ". Capacidad maxima de " + capacidad.getLimitePasteles() + " pasteles alcanzada.";

        return ValidarFechaResponse.builder()
                .fecha(fecha)
                .disponible(disponible)
                .cuposRestantes(disponibles)
                .limiteDiario(capacidad.getLimitePasteles())
                .mensaje(mensaje)
                .build();
    }

    @Transactional
    public PedidoResponse registrarPedido(CrearPedidoRequest request) {
        LocalDate fecha = request.getFechaEntrega();

        if (fecha.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("No se pueden registrar pedidos con fechas pasadas.");
        }

        CapacidadCocina capacidad = obtenerOCrearCapacidad(fecha);
        if (!capacidad.tieneCapacidad()) {
            throw new IllegalStateException("Lo sentimos, no hay cupos de produccion disponibles para el " + fecha +
                    ". Maximo diario: " + capacidad.getLimitePasteles() + " pasteles.");
        }
        Usuario usuario = obtenerOCrearUsuario(request);
        Pedido pedido = Pedido.builder()
                .usuario(usuario)
                .pastelNombre(request.getPastelNombre())
                .fechaEntrega(request.getFechaEntrega())
                .porciones(request.getPorciones())
                .restricciones(request.getRestricciones())
                .precioCop(request.getPrecioCop())
                .estado(EstadoPedido.CONFIRMADO)
                .build();

        Pedido pedidoGuardado = pedidoRepository.save(pedido);
        capacidad.registrarPastel();
        capacidadCocinaRepository.save(capacidad);

        return mapToResponse(pedidoGuardado, "Pedido #" + pedidoGuardado.getId() + " confirmado exitosamente para " +
                usuario.getNombre() + " el " + fecha + ".");
    }

    @Transactional(readOnly = true)
    public List<PedidoResponse> listarPedidos() {
        return pedidoRepository.findAll().stream()
                .map(p -> mapToResponse(p, "Pedido registrado"))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PedidoResponse> listarPedidosPorUsuario(Long usuarioId) {
        return pedidoRepository.findByUsuarioId(usuarioId).stream()
                .map(p -> mapToResponse(p, "Pedido del cliente"))
                .collect(Collectors.toList());
    }

    @Transactional
    public CapacidadCocina obtenerOCrearCapacidad(LocalDate fecha) {
        return capacidadCocinaRepository.findByFecha(fecha)
                .orElseGet(() -> capacidadCocinaRepository.save(
                        CapacidadCocina.builder()
                                .fecha(fecha)
                                .limitePasteles(LIMITE_POR_DEFECTO)
                                .pastelesProgramados(0)
                                .build()
                ));
    }

    private Usuario obtenerOCrearUsuario(CrearPedidoRequest req) {
        if (req.getClienteEmail() != null && !req.getClienteEmail().isBlank()) {
            return usuarioRepository.findByEmail(req.getClienteEmail())
                    .map(u -> {
                        u.setNombre(req.getClienteNombre());
                        if (req.getClienteTelefono() != null) u.setTelefono(req.getClienteTelefono());
                        if (req.getClienteDireccion() != null) u.setDireccion(req.getClienteDireccion());
                        return usuarioRepository.save(u);
                    })
                    .orElseGet(() -> usuarioRepository.save(
                            Usuario.builder()
                                    .nombre(req.getClienteNombre())
                                    .email(req.getClienteEmail())
                                    .telefono(req.getClienteTelefono())
                                    .direccion(req.getClienteDireccion())
                                    .build()
                    ));
        }
        if (req.getClienteTelefono() != null && !req.getClienteTelefono().isBlank()) {
            return usuarioRepository.findByTelefono(req.getClienteTelefono())
                    .orElseGet(() -> usuarioRepository.save(
                            Usuario.builder()
                                    .nombre(req.getClienteNombre())
                                    .telefono(req.getClienteTelefono())
                                    .direccion(req.getClienteDireccion())
                                    .build()
                    ));
        }

        return usuarioRepository.save(
                Usuario.builder()
                        .nombre(req.getClienteNombre())
                        .direccion(req.getClienteDireccion())
                        .build()
        );
    }

    private PedidoResponse mapToResponse(Pedido p, String mensaje) {
        Usuario u = p.getUsuario();
        return PedidoResponse.builder()
                .id(p.getId())
                .usuarioId(u != null ? u.getId() : null)
                .clienteNombre(u != null ? u.getNombre() : "Cliente")
                .clienteEmail(u != null ? u.getEmail() : null)
                .clienteTelefono(u != null ? u.getTelefono() : null)
                .pastelNombre(p.getPastelNombre())
                .fechaEntrega(p.getFechaEntrega())
                .porciones(p.getPorciones())
                .restricciones(p.getRestricciones())
                .precioCop(p.getPrecioCop())
                .estado(p.getEstado())
                .fechaCreacion(p.getFechaCreacion())
                .mensajeConfirmacion(mensaje)
                .build();
    }
}
