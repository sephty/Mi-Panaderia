package com.panaderia.mvp.repository;

import com.panaderia.mvp.model.EstadoPedido;
import com.panaderia.mvp.model.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    List<Pedido> findByFechaEntrega(LocalDate fechaEntrega);
    List<Pedido> findByUsuarioId(Long usuarioId);
    List<Pedido> findByEstado(EstadoPedido estado);
    long countByFechaEntregaAndEstadoNot(LocalDate fechaEntrega, EstadoPedido estado);
}
