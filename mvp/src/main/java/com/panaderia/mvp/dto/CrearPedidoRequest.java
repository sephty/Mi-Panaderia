package com.panaderia.mvp.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CrearPedidoRequest {

    @NotBlank(message = "El nombre del cliente es obligatorio")
    private String clienteNombre;

    private String clienteEmail;

    private String clienteTelefono;

    private String clienteDireccion;

    @NotBlank(message = "El nombre del pastel es obligatorio")
    private String pastelNombre;

    @NotNull(message = "La fecha de entrega es obligatoria")
    private LocalDate fechaEntrega;

    @NotNull(message = "El numero de porciones es obligatorio")
    @Min(value = 1, message = "El pedido debe ser de al menos 1 porcion")
    private Integer porciones;

    private String restricciones;

    private Double precioCop;
}
