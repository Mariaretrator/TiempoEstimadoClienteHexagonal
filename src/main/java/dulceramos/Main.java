package dulceramos;

import dulceramos.adaptadores.notificacion.AdaptadorNotificacionCliente;
import dulceramos.adaptadores.redes.AdaptadorClienteUdp;
import dulceramos.adaptadores.redes.CanalUdp;
import dulceramos.adaptadores.redes.ProtocoloUdpMapper;
import dulceramos.aplicacion.mapper.ClienteMapper;
import dulceramos.aplicacion.servicios.ClienteViajeService;
import dulceramos.ClienteFrame;

import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import javax.swing.SwingUtilities;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Main {
    public static void main(final String[] args) {
        final AdaptadorNotificacionCliente notificador = new AdaptadorNotificacionCliente();
        final AdaptadorClienteUdp udp = new AdaptadorClienteUdp(new CanalUdp(3000), new ProtocoloUdpMapper());

        final ExecutorService executor = Executors.newSingleThreadExecutor(tarea -> {
            final Thread hilo = new Thread(tarea, "cliente-udp");
            hilo.setDaemon(true);
            return hilo;
        });

        // Pasamos los 4 argumentos exactos requeridos por ClienteViajeService
        final ClienteViajeService servicio = new ClienteViajeService(udp, notificador, new ClienteMapper(), executor);

        SwingUtilities.invokeLater(() -> {
            final ClienteFrame frame = new ClienteFrame(servicio, servicio);
            notificador.registrar(frame);
            frame.setVisible(true);
        });
    }
}