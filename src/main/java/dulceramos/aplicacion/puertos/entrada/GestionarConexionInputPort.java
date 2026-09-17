package dulceramos.aplicacion.puertos.entrada;

import dulceramos.aplicacion.dto.ConectarCommand;

import java.util.concurrent.CompletableFuture;

public interface GestionarConexionInputPort {
    CompletableFuture<Void> conectar(ConectarCommand comando);

    CompletableFuture<Void> desconectar();

    boolean estaConectado();
}