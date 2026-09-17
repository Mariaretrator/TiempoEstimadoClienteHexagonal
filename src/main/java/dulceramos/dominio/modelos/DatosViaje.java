package dulceramos.dominio.modelos;

import dulceramos.dominio.vo.Distancia;
import dulceramos.dominio.vo.Velocidad;

import java.util.Objects;

public record DatosViaje(Distancia distancia, Velocidad velocidad) {
    public DatosViaje {
        Objects.requireNonNull(distancia, "La distancia es obligatoria.");
        Objects.requireNonNull(velocidad, "La velocidad es obligatoria.");
    }
}