package dulceramos.dominio.excepciones;

import java.io.Serial;

public final class RespuestaServidorException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;

    public RespuestaServidorException(final String mensaje) {
        super(mensaje);
    }

    public RespuestaServidorException(final String mensaje, final Throwable causa) {
        super(mensaje, causa);
    }
}