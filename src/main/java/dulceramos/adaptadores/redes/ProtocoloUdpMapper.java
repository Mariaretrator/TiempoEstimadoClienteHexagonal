package dulceramos.adaptadores.redes;

import dulceramos.dominio.excepciones.RespuestaServidorException;
import dulceramos.dominio.modelos.DatosViaje;
import dulceramos.dominio.modelos.ResultadoViaje;

import java.util.Locale;
import java.util.Objects;

public final class ProtocoloUdpMapper {
    public static final String CONECTAR = "CONECTAR";
    public static final String DESCONECTAR = "DESCONECTAR";

    public String serializarCalculo(final DatosViaje datos) {
        Objects.requireNonNull(datos, "Los datos del viaje son obligatorios.");
        return String.format(Locale.US, "CALCULAR;%.2f;%.2f", datos.distancia().valor(), datos.velocidad().valor());
    }

    public void validarConexion(final String respuesta) {
        if (Objects.isNull(respuesta) || !respuesta.startsWith("CONECTADO_OK;")) {
            throw new RespuestaServidorException("Respuesta de conexión inesperada: " + respuesta);
        }
    }

    public ResultadoViaje parsearCalculo(final String respuesta) {
        // 1. EL CHISMOSO: Imprimirá en la consola negra exactamente qué mandó el servidor
        System.out.println("-> RESPUESTA CRUDA DEL SERVIDOR: " + respuesta);

        if (Objects.isNull(respuesta) || respuesta.isBlank()) {
            throw new RespuestaServidorException("El servidor envió una respuesta vacía.");
        }
        if (respuesta.startsWith("ERROR;")) {
            throw new RespuestaServidorException("Error del servidor: " + respuesta.substring(6));
        }

        final String[] partes = respuesta.split(";", -1);

        // 2. Relajamos la validación: con que traiga el comando y el tiempo (2 partes) nos damos por bien servidos
        if (partes.length < 2) {
            throw new RespuestaServidorException("Respuesta de cálculo muy corta: " + respuesta);
        }

        // Permitimos OK_CALCULO o el viejo OK_IMC temporalmente para no romper
        if (!"OK_CALCULO".equals(partes[0]) && !"OK_IMC".equals(partes[0])) {
            throw new RespuestaServidorException("Comando desconocido del servidor: " + respuesta);
        }

        try {
            final double tiempoHoras = Double.parseDouble(partes[1].replace(',', '.'));

            // 3. Formateo inteligente para mostrar "1 hora" o "X horas" de forma natural
            final String tiempoFormateado;
            if (tiempoHoras == 1.0) {
                tiempoFormateado = "1 hora";
            } else if (tiempoHoras == (int) tiempoHoras) {
                tiempoFormateado = (int) tiempoHoras + " horas";
            } else {
                tiempoFormateado = tiempoHoras + " horas";
            }

            // 4. Asignamos la descripción y observaciones con respaldo seguro
            final String descripcionRuta = partes.length > 2 && !partes[2].isBlank() ? partes[2] : "Trayecto calculado";
            final String observaciones = partes.length > 3 ? partes[3] : "";

            return new ResultadoViaje(tiempoHoras, tiempoFormateado, descripcionRuta, observaciones);
        } catch (final NumberFormatException excepcion) {
            throw new RespuestaServidorException("El tiempo estimado recibido no es numérico: " + partes[1]);
        }
    }
}