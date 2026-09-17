package dulceramos;

import dulceramos.aplicacion.dto.CalcularTiempoCommand;
import dulceramos.aplicacion.dto.ConectarCommand;
import dulceramos.aplicacion.excepciones.ClienteRedException;
import dulceramos.aplicacion.mapper.ClienteMapper;
import dulceramos.aplicacion.servicios.ClienteViajeService;
import dulceramos.dominio.enums.EstadoConexion;
import dulceramos.dominio.modelos.DatosViaje;
import dulceramos.dominio.modelos.EventoCliente;
import dulceramos.dominio.modelos.ResultadoViaje;
import dulceramos.dominio.puertos.salida.ClienteUdpPort;
import dulceramos.dominio.puertos.salida.PuertoNotificacionCliente;
import dulceramos.dominio.vo.DestinoServidor;
import dulceramos.dominio.vo.Distancia;
import dulceramos.dominio.vo.Velocidad;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClienteServiceTest {
    @Test
    void orquestaConexionCalculoYDesconexion() {
        final UdpMemoria udp = new UdpMemoria();
        final NotificadorMemoria notificador = new NotificadorMemoria();
        final ClienteViajeService servicio = new ClienteViajeService(udp, notificador, new ClienteMapper(), Runnable::run);
        servicio.conectar(new ConectarCommand("localhost", 9876)).join();
        assertTrue(servicio.estaConectado());
        assertEquals(List.of(EstadoConexion.CONECTANDO, EstadoConexion.CONECTADO), notificador.estados);
        assertEquals(2.0, servicio.calcular(new CalcularTiempoCommand(100.0, 50.0)).join().tiempoHoras());
        assertEquals(1, notificador.resultados.size());
        servicio.desconectar().join();
        assertFalse(servicio.estaConectado());
        assertEquals(EstadoConexion.DESCONECTADO, notificador.estados.get(2));
    }

    @Test
    void notificaErroresAsincronos() {
        final UdpMemoria udp = new UdpMemoria();
        udp.fallar = true;
        final NotificadorMemoria notificador = new NotificadorMemoria();
        final ClienteViajeService servicio = new ClienteViajeService(udp, notificador, new ClienteMapper(), Runnable::run);
        assertThrows(CompletionException.class, () -> servicio.conectar(new ConectarCommand("localhost", 9876)).join());
        assertFalse(notificador.errores.isEmpty());
    }

    @Test
    void notificaErroresDeCalculoYDesconexion() {
        final UdpMemoria udp = new UdpMemoria();
        final NotificadorMemoria notificador = new NotificadorMemoria();
        final ClienteViajeService servicio = new ClienteViajeService(udp, notificador, new ClienteMapper(), Runnable::run);
        udp.fallarCalculo = true;
        assertThrows(CompletionException.class, () -> servicio.calcular(new CalcularTiempoCommand(100.0, 50.0)).join());
        udp.fallarDesconexion = true;
        assertThrows(CompletionException.class, () -> servicio.desconectar().join());
        assertEquals(2, notificador.errores.size());
    }

    private static final class UdpMemoria implements ClienteUdpPort {
        private boolean conectado;
        private boolean fallar;
        private boolean fallarCalculo;
        private boolean fallarDesconexion;

        @Override
        public void conectar(final DestinoServidor destino) throws ClienteRedException {
            if (fallar)
                throw new ClienteRedException("fallo");
            conectado = true;
        }

        @Override
        public void desconectar() throws ClienteRedException {
            if (fallarDesconexion)
                throw new ClienteRedException("fallo desconexión");
            conectado = false;
        }

        @Override
        public ResultadoViaje solicitarCalculo(final DatosViaje datos) throws ClienteRedException {
            if (fallarCalculo)
                throw new ClienteRedException("fallo cálculo");
            return new ResultadoViaje(2.0, "2.0 horas", "Ruta Principal", "Sin observaciones");
        }

        @Override
        public boolean estaConectado() {
            return conectado;
        }
    }

    private static final class NotificadorMemoria implements PuertoNotificacionCliente {
        private final List<EstadoConexion> estados = new ArrayList<>();
        private final List<ResultadoViaje> resultados = new ArrayList<>();
        private final List<String> errores = new ArrayList<>();

        @Override
        public void notificarEstado(final EstadoConexion estado, final String endpoint) {
            estados.add(estado);
        }

        @Override
        public void notificarEvento(final EventoCliente evento) {
        }

        @Override
        public void notificarResultado(final ResultadoViaje resultado) {
            resultados.add(resultado);
        }

        @Override
        public void notificarError(final String mensaje) {
            errores.add(mensaje);
        }
    }
}