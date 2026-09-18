package com.panaderia.mvp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ValidarFechaResponse {
    private LocalDate fecha;
    private boolean disponible;
    private int cuposRestantes;
    private int limiteDiario;
    private String mensaje;
}
