package dulceramos.aplicacion.mapper;

import dulceramos.aplicacion.dto.CalcularTiempoCommand;
import dulceramos.aplicacion.dto.ConectarCommand;
import dulceramos.dominio.modelos.DatosViaje;
import dulceramos.dominio.vo.DestinoServidor;
import dulceramos.dominio.vo.Distancia;
import dulceramos.dominio.vo.Velocidad;

import java.util.Objects;

public final class ClienteMapper {
    public DestinoServidor toDestino(final ConectarCommand comando) {
        Objects.requireNonNull(comando, "El comando de conexión es obligatorio.");
        return new DestinoServidor(comando.host(), comando.puerto());
    }

    public DatosViaje toDatos(final CalcularTiempoCommand comando) {
        Objects.requireNonNull(comando, "El comando de cálculo es obligatorio.");
        return new DatosViaje(new Distancia(comando.distancia()), new Velocidad(comando.velocidad()));
    }
}
