package dulceramos.dominio.puertos.salida;

import dulceramos.dominio.enums.EstadoConexion;
import dulceramos.dominio.modelos.EventoCliente;
import dulceramos.dominio.modelos.ResultadoViaje;

public interface PuertoNotificacionCliente {
    void notificarEstado(EstadoConexion estado, String endpoint);

    void notificarEvento(EventoCliente evento);

    void notificarResultado(ResultadoViaje resultado);

    void notificarError(String mensaje);
}