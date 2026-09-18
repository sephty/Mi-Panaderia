package com.panaderia.mvp.repository;

import com.panaderia.mvp.model.CapacidadCocina;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface CapacidadCocinaRepository extends JpaRepository<CapacidadCocina, Long> {
    Optional<CapacidadCocina> findByFecha(LocalDate fecha);
}
