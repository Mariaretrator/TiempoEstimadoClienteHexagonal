package dulceramos;

import dulceramos.adaptadores.red.ProtocoloUdpMapper;
import dulceramos.aplicacion.dto.CalcularTiempoCommand;
import dulceramos.aplicacion.dto.ConectarCommand;
import dulceramos.aplicacion.mapper.ClienteMapper;
import dulceramos.dominio.excepciones.VelocidadIncorrectaException;
import dulceramos.dominio.excepciones.DistanciaIncorrectaException;
import dulceramos.dominio.excepciones.RespuestaServidorException;
import dulceramos.dominio.modelos.DatosViaje;
import dulceramos.dominio.modelos.ResultadoViaje;
import dulceramos.dominio.vo.Velocidad;
import dulceramos.dominio.vo.Distancia;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DominioYProtocoloTest {
    @Test
    void validaValores() {
        assertThrows(DistanciaIncorrectaException.class, () -> new Distancia(0));
        assertThrows(DistanciaIncorrectaException.class, () -> new Distancia(Double.NaN));
        assertThrows(VelocidadIncorrectaException.class, () -> new Velocidad(-1));
        assertThrows(VelocidadIncorrectaException.class, () -> new Velocidad(Double.POSITIVE_INFINITY));
        assertThrows(DestinoIncorrectoException.class,
                () -> new ClienteMapper().toDestino(new ConectarCommand(" ", 9876)));
        assertThrows(DestinoIncorrectoException.class,
                () -> new ClienteMapper().toDestino(new ConectarCommand("host", 80)));
    }

    @Test
    void validaIntegridadDelResultado() {
        assertThrows(RespuestaServidorException.class, () -> new ResultadoViaje(Double.NaN, "x", "Ruta Principal", "ok"));
        assertThrows(RespuestaServidorException.class, () -> new ResultadoViaje(2.0, " ", "Ruta Principal", "ok"));
        assertThrows(RespuestaServidorException.class, () -> new ResultadoViaje(2.0, "2.0 horas", " ", "ok"));
        assertThrows(RespuestaServidorException.class, () -> new ResultadoViaje(2.0, "2.0 horas", "Ruta Principal", null));
    }

    @Test
    void transformaComandos() {
        final ClienteMapper mapper = new ClienteMapper();
        assertEquals("localhost:9876", mapper.toDestino(new ConectarCommand(" localhost ", 9876)).endpoint());
        final DatosViaje datos = mapper.toDatos(new CalcularTiempoCommand(100.0, 50.0));
        assertEquals("CALCULAR;100.00;50.00", new ProtocoloUdpMapper().serializarCalculo(datos));
    }

    @Test
    void parseaResultadoYErrores() {
        final ProtocoloUdpMapper protocolo = new ProtocoloUdpMapper();
        protocolo.validarConexion("CONECTADO_OK;listo");
        final var resultado = protocolo.parsearCalculo("OK_CALCULO;2.0;2.0 horas;Ruta Principal;Sin observaciones");
        assertEquals(2.0, resultado.tiempoHoras());
        assertEquals("Ruta Principal", resultado.descripcionRuta());
        assertThrows(RespuestaServidorException.class, () -> protocolo.validarConexion("ERROR"));
        assertThrows(RespuestaServidorException.class, () -> protocolo.parsearCalculo(null));
        assertThrows(RespuestaServidorException.class, () -> protocolo.parsearCalculo("  "));
        assertThrows(RespuestaServidorException.class, () -> protocolo.parsearCalculo("ERROR;fallo"));
        assertThrows(RespuestaServidorException.class, () -> protocolo.parsearCalculo("OTRA"));
        assertThrows(RespuestaServidorException.class, () -> protocolo.parsearCalculo("OK_CALCULO;x;2.0 horas;Ruta Principal;ok"));
    }
}