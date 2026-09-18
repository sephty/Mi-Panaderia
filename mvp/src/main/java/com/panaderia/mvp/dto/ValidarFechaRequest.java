package com.panaderia.mvp.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValidarFechaRequest {

    @NotNull(message = "La fecha de entrega es obligatoria")
    private LocalDate fechaEntrega;
}
