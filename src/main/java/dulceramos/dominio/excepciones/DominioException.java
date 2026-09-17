package dulceramos.dominio.excepciones;

public class DominioException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public DominioException(final String mensaje) {
        super(mensaje);
    }

    public DominioException(final String mensaje, final Throwable causa) {
        super(mensaje, causa);
    }
}
