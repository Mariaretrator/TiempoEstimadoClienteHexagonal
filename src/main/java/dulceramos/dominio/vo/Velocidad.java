package dulceramos.dominio.vo;

import dulceramos.dominio.excepciones.VelocidadIncorrectaException;

public record Velocidad(double valor) {
    public Velocidad {
        if (!Double.isFinite(valor) || valor <= 0) {
            throw new VelocidadIncorrectaException("La velocidad debe ser un número finito mayor que cero.");
        }
    }
}