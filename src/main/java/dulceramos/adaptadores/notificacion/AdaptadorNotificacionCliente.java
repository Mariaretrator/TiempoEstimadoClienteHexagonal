package dulceramos.adaptadores.notificacion;

import dulceramos.dominio.enums.EstadoConexion;
import dulceramos.dominio.modelos.EventoCliente;
import dulceramos.dominio.modelos.ResultadoViaje; // <--- Cambiado al modelo de tu dominio
import dulceramos.dominio.puertos.salida.PuertoNotificacionCliente;

import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

public final class AdaptadorNotificacionCliente implements PuertoNotificacionCliente {
    private final CopyOnWriteArrayList<ObservadorCliente> observadores = new CopyOnWriteArrayList<>();

    public void registrar(final ObservadorCliente observador) {
        observadores.addIfAbsent(Objects.requireNonNull(observador, "El observador es obligatorio."));
    }

    public void remover(final ObservadorCliente observador) {
        observadores.remove(observador);
    }

    @Override
    public void notificarEstado(final EstadoConexion estado, final String endpoint) {
        Objects.requireNonNull(estado, "El estado es obligatorio.");
        Objects.requireNonNull(endpoint, "El endpoint es obligatorio.");
        observadores.forEach(o -> o.onEstado(estado, endpoint));
    }

    @Override
    public void notificarEvento(final EventoCliente evento) {
        Objects.requireNonNull(evento, "El evento es obligatorio.");
        observadores.forEach(o -> o.onEvento(evento));
    }


    public void notificarResultado(final ResultadoViaje resultado) { // <--- Actualizado aquí
        Objects.requireNonNull(resultado, "El resultado es obligatorio.");
        observadores.forEach(o -> o.onResultado(resultado));
    }


    public void notificarError(final String mensaje) {
        Objects.requireNonNull(mensaje, "El mensaje es obligatorio.");
        observadores.forEach(o -> o.onError(mensaje));
    }
}
