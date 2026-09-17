package dulceramos.dominio.vo;

import dulceramos.dominio.excepciones.DistanciaIncorrectaException;

public record Distancia(double valor) {
    public Distancia {
        if (!Double.isFinite(valor) || valor <= 0) {
            throw new DistanciaIncorrectaException("La distancia debe ser un número finito mayor que cero.");
        }
    }
}