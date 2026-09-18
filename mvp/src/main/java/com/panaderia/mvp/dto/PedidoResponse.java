package com.panaderia.mvp.dto;

import com.panaderia.mvp.model.EstadoPedido;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PedidoResponse {
    private Long id;
    private Long usuarioId;
    private String clienteNombre;
    private String clienteEmail;
    private String clienteTelefono;
    private String pastelNombre;
    private LocalDate fechaEntrega;
    private Integer porciones;
    private String restricciones;
    private Double precioCop;
    private EstadoPedido estado;
    private LocalDateTime fechaCreacion;
    private String mensajeConfirmacion;
}
