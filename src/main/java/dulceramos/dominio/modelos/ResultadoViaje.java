package dulceramos.dominio.modelos;

import dulceramos.dominio.excepciones.RespuestaServidorException;

import java.util.Objects;

public record ResultadoViaje(double tiempoHoras, String tiempoFormateado, String descripcionRuta, String observaciones) {
    public ResultadoViaje {
        if (!Double.isFinite(tiempoHoras) || Objects.isNull(tiempoFormateado) || tiempoFormateado.isBlank()
                || Objects.isNull(descripcionRuta) || descripcionRuta.isBlank()
                || Objects.isNull(observaciones)) {
            throw new RespuestaServidorException("El resultado del viaje recibido está incompleto o es inválido.");
        }
    }
}
