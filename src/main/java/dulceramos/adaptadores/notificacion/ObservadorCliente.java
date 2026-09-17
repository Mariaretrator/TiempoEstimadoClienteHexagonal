package dulceramos.adaptadores.notificacion;

import dulceramos.dominio.enums.EstadoConexion;
import dulceramos.dominio.modelos.EventoCliente;
import dulceramos.dominio.modelos.ResultadoViaje;

public interface ObservadorCliente {
    void onEstado(EstadoConexion estado, String endpoint);

    void onEvento(EventoCliente evento);

    void onResultado(ResultadoViaje resultado);

    void onError(String mensaje);
}
