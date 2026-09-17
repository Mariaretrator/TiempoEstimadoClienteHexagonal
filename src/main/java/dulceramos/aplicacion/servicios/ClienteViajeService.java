package dulceramos.aplicacion.servicios;

import dulceramos.aplicacion.dto.CalcularTiempoCommand;
import dulceramos.aplicacion.dto.ConectarCommand;
import dulceramos.aplicacion.excepciones.ClienteRedException;
import dulceramos.aplicacion.mapper.ClienteMapper;
import dulceramos.aplicacion.puertos.entrada.CalcularTiempoInputPort;
import dulceramos.aplicacion.puertos.entrada.GestionarConexionInputPort;
import dulceramos.dominio.enums.EstadoConexion;
import dulceramos.dominio.modelos.EventoCliente;
import dulceramos.dominio.modelos.ResultadoViaje;
import dulceramos.dominio.puertos.salida.ClienteUdpPort;
import dulceramos.dominio.puertos.salida.PuertoNotificacionCliente;
import dulceramos.dominio.vo.DestinoServidor;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RequiredArgsConstructor
public final class ClienteViajeService implements GestionarConexionInputPort, CalcularTiempoInputPort {
    private static final Logger LOG = LoggerFactory.getLogger(ClienteViajeService.class);
    private static final String LOG_ERROR_OPERACION = "Falló la operación UDP {}";
    private static final String OPERACION_CONECTAR = "conectar";
    private static final String OPERACION_DESCONECTAR = "desconectar";
    private static final String OPERACION_CALCULAR = "calcular";
    private final ClienteUdpPort clienteUdp;
    private final PuertoNotificacionCliente notificador;
     private final ClienteMapper mapper;
     private final Executor executor;

    @Override
    public CompletableFuture<Void> conectar(final ConectarCommand comando) {
        final DestinoServidor destino = mapper.toDestino(comando);
        notificador.notificarEstado(EstadoConexion.CONECTANDO, destino.endpoint());
        return CompletableFuture.runAsync(() -> {
            try {
                clienteUdp.conectar(destino);
                notificador.notificarEstado(EstadoConexion.CONECTADO, destino.endpoint());
                notificador.notificarEvento(new EventoCliente("CONEXIÓN", "Conectado a " + destino.endpoint()));
            } catch (final ClienteRedException excepcion) {
                LOG.error(LOG_ERROR_OPERACION, OPERACION_CONECTAR, excepcion);
                notificador.notificarEstado(EstadoConexion.DESCONECTADO, destino.endpoint());
                notificador.notificarError(excepcion.getMessage());
                throw new CompletionException(excepcion);
            }
        }, executor);
    }

    @Override
    public CompletableFuture<Void> desconectar() {
        return CompletableFuture.runAsync(() -> {
            try {
                clienteUdp.desconectar();
                notificador.notificarEstado(EstadoConexion.DESCONECTADO, "");
                notificador.notificarEvento(new EventoCliente("CONEXIÓN", "Cliente desconectado."));
            } catch (final ClienteRedException excepcion) {
                LOG.error(LOG_ERROR_OPERACION, OPERACION_DESCONECTAR, excepcion);
                notificador.notificarError(excepcion.getMessage());
                throw new CompletionException(excepcion);
            }
        }, executor);
    }

    @Override
    public CompletableFuture<ResultadoViaje> calcular(final CalcularTiempoCommand comando) {
        final var datos = mapper.toDatos(comando);
        return CompletableFuture.supplyAsync(() -> {
            try {
                final ResultadoViaje resultado = clienteUdp.solicitarCalculo(datos);
                notificador.notificarResultado(resultado);
                return resultado;
            } catch (final ClienteRedException excepcion) {
                LOG.error(LOG_ERROR_OPERACION, OPERACION_CALCULAR, excepcion);
                notificador.notificarError(excepcion.getMessage());
                throw new CompletionException(excepcion);
            }
        }, executor);
    }

    @Override
    public boolean estaConectado() {
        return clienteUdp.estaConectado();
    }
}
