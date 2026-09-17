package dulceramos.aplicacion.puertos.entrada;

import dulceramos.aplicacion.dto.CalcularTiempoCommand;
import dulceramos.dominio.modelos.ResultadoViaje;

import java.util.concurrent.CompletableFuture;

public interface CalcularTiempoInputPort {
    CompletableFuture<ResultadoViaje> calcular(CalcularTiempoCommand comando);
}