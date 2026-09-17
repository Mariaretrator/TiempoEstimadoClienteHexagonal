package dulceramos.dominio.puertos.salida;

import dulceramos.aplicacion.excepciones.ClienteRedException;
import dulceramos.dominio.modelos.DatosViaje;
import dulceramos.dominio.modelos.ResultadoViaje;
import dulceramos.dominio.vo.DestinoServidor;

public interface ClienteUdpPort {
    void conectar(DestinoServidor destino) throws ClienteRedException;

    void desconectar() throws ClienteRedException;

    ResultadoViaje solicitarCalculo(DatosViaje datos) throws ClienteRedException;

    boolean estaConectado();
}
