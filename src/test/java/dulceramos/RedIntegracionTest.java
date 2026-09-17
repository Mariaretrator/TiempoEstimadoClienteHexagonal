package dulceramos;

import dulceramos.adaptadores.red.AdaptadorClienteUdp;
import dulceramos.adaptadores.red.CanalUdp;
import dulceramos.adaptadores.red.ProtocoloUdpMapper;
import dulceramos.aplicacion.excepciones.ClienteRedException;
import dulceramos.dominio.modelos.DatosViaje;
import dulceramos.dominio.vo.Distancia;
import dulceramos.dominio.vo.Velocidad;
import dulceramos.dominio.vo.DestinoServidor;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RedIntegracionTest {
    @Test
    void intercambiaProtocoloCompletoConServidorUdpReal()
            throws IOException, ClienteRedException, InterruptedException {
        try (DatagramSocket servidor = new DatagramSocket(0)) {
            final CountDownLatch terminado = new CountDownLatch(1);
            final Thread hilo = new Thread(() -> atender(servidor, terminado));
            hilo.start();
            final AdaptadorClienteUdp cliente = new AdaptadorClienteUdp(new CanalUdp(2000), new ProtocoloUdpMapper());
            cliente.conectar(new DestinoServidor("127.0.0.1", servidor.getLocalPort()));
            assertTrue(cliente.estaConectado());
            final var resultado = cliente.solicitarCalculo(new DatosViaje(new Distancia(100.0), new Velocidad(50.0)));
            assertEquals(2.0, resultado.tiempoHoras());
            cliente.desconectar();
            assertFalse(cliente.estaConectado());
            assertTrue(terminado.await(2, TimeUnit.SECONDS));
        }
    }

    @Test
    void rechazaCalculoSinConexionYHostDesconocido() {
        final AdaptadorClienteUdp cliente = new AdaptadorClienteUdp(new CanalUdp(100), new ProtocoloUdpMapper());
        assertThrows(ClienteRedException.class,
                () -> cliente.solicitarCalculo(new DatosViaje(new Distancia(100.0), new Velocidad(50.0))));
        assertThrows(ClienteRedException.class, () -> cliente.conectar(new DestinoServidor("host.invalid", 9876)));
    }

    private static void atender(final DatagramSocket socket, final CountDownLatch terminado) {
        try {
            responder(socket, "CONECTADO_OK;listo");
            responder(socket, "OK_CALCULO;2.0;2.0 horas;Ruta Principal;Sin observaciones");
            final DatagramPacket cierre = new DatagramPacket(new byte[64], 64);
            socket.receive(cierre);
            terminado.countDown();
        } catch (final IOException excepcion) {
            throw new AssertionError(excepcion);
        }
    }

    private static void responder(final DatagramSocket socket, final String respuesta) throws IOException {
        final DatagramPacket entrada = new DatagramPacket(new byte[256], 256);
        socket.receive(entrada);
        final byte[] datos = respuesta.getBytes(StandardCharsets.UTF_8);
        socket.send(new DatagramPacket(datos, datos.length, entrada.getAddress(), entrada.getPort()));
    }
}