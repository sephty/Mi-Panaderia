package com.panaderia.mvp.config;

import com.panaderia.mvp.dto.CrearPedidoRequest;
import com.panaderia.mvp.model.CapacidadCocina;
import com.panaderia.mvp.model.Usuario;
import com.panaderia.mvp.repository.CapacidadCocinaRepository;
import com.panaderia.mvp.repository.UsuarioRepository;
import com.panaderia.mvp.service.CocinaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final CocinaService cocinaService;
    private final UsuarioRepository usuarioRepository;
    private final CapacidadCocinaRepository capacidadCocinaRepository;

    @Override
    public void run(String... args) {
        if (usuarioRepository.count() == 0) {
            log.info("Inicializando datos demo de prueba en H2...");
            Usuario clienteDemo = usuarioRepository.save(
                    Usuario.builder()
                            .nombre("Camila Morales")
                            .email("camila.morales@example.com")
                            .telefono("+57 311 555 4321")
                            .direccion("Calle 45 #12-34, Bucaramanga")
                            .build()
            );
            LocalDate fechaEntregaDemo = LocalDate.now().plusDays(2);
            capacidadCocinaRepository.save(
                    CapacidadCocina.builder()
                            .fecha(fechaEntregaDemo)
                            .limitePasteles(5)
                            .pastelesProgramados(0)
                            .build()
            );
            CrearPedidoRequest pedidoDemo = new CrearPedidoRequest(
                    clienteDemo.getNombre(),
                    clienteDemo.getEmail(),
                    clienteDemo.getTelefono(),
                    clienteDemo.getDireccion(),
                    "Cheesecake de Frutos Rojos Sin Gluten",
                    fechaEntregaDemo,
                    10,
                    "Sin gluten (celiaco)",
                    95000.0
            );
            cocinaService.registrarPedido(pedidoDemo);

            log.info("Datos demo inicializados con exito. Pedido demo registrado para: {}", clienteDemo.getNombre());
        }
    }
}
