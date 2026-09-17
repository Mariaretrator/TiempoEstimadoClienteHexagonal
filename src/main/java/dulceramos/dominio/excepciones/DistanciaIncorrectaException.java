package dulceramos.dominio.excepciones;

import java.io.Serial;

public final class DistanciaIncorrectaException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;

    public DistanciaIncorrectaException(final String mensaje) {
        super(mensaje);
    }
}
