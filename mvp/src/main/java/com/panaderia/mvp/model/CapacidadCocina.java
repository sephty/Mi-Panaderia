package com.panaderia.mvp.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "capacidad_cocina")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CapacidadCocina {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private LocalDate fecha;

    @Column(name = "limite_pasteles", nullable = false)
    @Builder.Default
    private Integer limitePasteles = 5;

    @Column(name = "pasteles_programados", nullable = false)
    @Builder.Default
    private Integer pastelesProgramados = 0;

    public boolean tieneCapacidad() {
        return (pastelesProgramados < limitePasteles);
    }

    public int cuposDisponibles() {
        return Math.max(0, limitePasteles - pastelesProgramados);
    }

    public void registrarPastel() {
        this.pastelesProgramados++;
    }
}
