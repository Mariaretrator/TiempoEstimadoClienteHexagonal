package dulceramos.dominio.excepciones;

public final class VelocidadIncorrectaException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public VelocidadIncorrectaException(final String mensaje) {
        super(mensaje);
    }
}