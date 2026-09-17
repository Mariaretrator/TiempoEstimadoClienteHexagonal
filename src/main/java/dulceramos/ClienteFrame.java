package dulceramos;

import dulceramos.adaptadores.notificacion.ObservadorCliente;
import dulceramos.adaptadores.red.util.RedUtil;
import dulceramos.aplicacion.dto.CalcularTiempoCommand;
import dulceramos.aplicacion.dto.ConectarCommand;
import dulceramos.aplicacion.puertos.entrada.CalcularTiempoInputPort;
import dulceramos.aplicacion.puertos.entrada.GestionarConexionInputPort;
import dulceramos.dominio.enums.EstadoConexion;
import dulceramos.dominio.excepciones.DominioException;
import dulceramos.dominio.modelos.EventoCliente;
import dulceramos.dominio.modelos.ResultadoViaje;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import java.io.Serial;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public final class ClienteFrame extends JFrame implements ObservadorCliente {
    @Serial
    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final transient GestionarConexionInputPort conexion;
    private final transient CalcularTiempoInputPort calculo;
    private final JTextField host = new JTextField("127.0.0.1");
    private final JTextField puerto = new JTextField("9876");
    private final JTextField distancia = new JTextField();
    private final JTextField velocidad = new JTextField();
    private final JButton conectar = new JButton("Conectar");
    private final JButton calcular = new JButton("Calcular Tiempo");
    private final JLabel estado = new JLabel("● DESCONECTADO");
    private final JLabel resultado = new JLabel("Tiempo: --");
    private final JTextArea logs = new JTextArea();

    public ClienteFrame(final GestionarConexionInputPort conexion, final CalcularTiempoInputPort calculo) {
        super("Cliente UDP - Cálculo de Tiempo de Viaje (Arquitectura Hexagonal)");
        this.conexion = Objects.requireNonNull(conexion);
        this.calculo = Objects.requireNonNull(calculo);
        construirInterfaz();
    }

    private void construirInterfaz() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(780, 580); // Ligeramente más alto para acomodar el nuevo diseño
        setLocationRelativeTo(null);

        final JPanel red = new JPanel(new GridLayout(3, 2, 8, 8));
        red.setBorder(BorderFactory.createTitledBorder("Red - IP local: " + RedUtil.obtenerIpLocal()));
        red.add(new JLabel("Host del servidor:"));
        red.add(host);
        red.add(new JLabel("Puerto:"));
        red.add(puerto);
        red.add(estado);
        red.add(conectar);

        // Panel de datos rediseñado para que el botón quede abajo y no tape el resultado
        final JPanel datos = new JPanel(new BorderLayout(8, 8));
        datos.setBorder(BorderFactory.createTitledBorder("Datos de Viaje"));

        final JPanel panelCampos = new JPanel(new GridLayout(2, 2, 8, 8));
        panelCampos.add(new JLabel("Distancia (km):"));
        panelCampos.add(distancia);
        panelCampos.add(new JLabel("Velocidad (km/h):"));
        panelCampos.add(velocidad);

        final JPanel panelInferior = new JPanel(new BorderLayout(0, 8));
        panelInferior.add(resultado, BorderLayout.NORTH);
        panelInferior.add(calcular, BorderLayout.CENTER);

        datos.add(panelCampos, BorderLayout.NORTH);
        datos.add(panelInferior, BorderLayout.CENTER);

        final JPanel centro = new JPanel(new GridLayout(2, 1, 8, 8));
        centro.add(red);
        centro.add(datos);

        logs.setEditable(false);
        final JButton limpiar = new JButton("Limpiar log");
        limpiar.addActionListener(evento -> logs.setText(""));

        final JPanel sur = new JPanel(new BorderLayout());
        sur.add(limpiar, BorderLayout.NORTH);
        sur.add(new JScrollPane(logs), BorderLayout.CENTER);

        final JPanel contenido = new JPanel(new BorderLayout(10, 10));
        contenido.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        contenido.add(centro, BorderLayout.NORTH);
        contenido.add(sur, BorderLayout.CENTER);

        add(contenido);
        conectar.addActionListener(evento -> alternarConexion());
        calcular.addActionListener(evento -> solicitarCalculo());
        habilitarCalculo(false);
    }

    private void alternarConexion() {
        if (conexion.estaConectado()) {
            conexion.desconectar();
            return;
        }
        try {
            conexion.conectar(new ConectarCommand(host.getText(), Integer.parseInt(puerto.getText().trim())));
        } catch (final NumberFormatException | DominioException excepcion) {
            mostrarError(excepcion.getMessage());
        }
    }

    private void solicitarCalculo() {
        try {
            final double valorDistancia = Double.parseDouble(distancia.getText().trim().replace(',', '.'));
            final double valorVelocidad = Double.parseDouble(velocidad.getText().trim().replace(',', '.'));
            calcular.setEnabled(false);
            calculo.calcular(new CalcularTiempoCommand(valorDistancia, valorVelocidad));
        } catch (final NumberFormatException | DominioException excepcion) {
            mostrarError(excepcion.getMessage());
        }
    }

    @Override
    public void onEstado(final EstadoConexion nuevoEstado, final String endpoint) {
        SwingUtilities.invokeLater(() -> {
            estado.setText("● " + nuevoEstado + (endpoint.isBlank() ? "" : " - " + endpoint));
            final boolean conectado = nuevoEstado == EstadoConexion.CONECTADO;
            host.setEditable(!conectado);
            puerto.setEditable(!conectado);
            conectar.setText(conectado ? "Desconectar" : "Conectar");
            habilitarCalculo(conectado);
        });
    }

    @Override
    public void onEvento(final EventoCliente evento) {
        SwingUtilities.invokeLater(() -> logs.append("[" + evento.fechaHora().format(FECHA) + "] ["
                + evento.categoria() + "] " + evento.descripcion() + System.lineSeparator()));
    }

    @Override
    public void onResultado(final ResultadoViaje valor) {
        SwingUtilities.invokeLater(() -> {
            resultado.setText("Tiempo: " + valor.tiempoFormateado() + " - " + valor.descripcionRuta());
            calcular.setEnabled(true);
        });
    }

    @Override
    public void onError(final String mensaje) {
        SwingUtilities.invokeLater(() -> {
            habilitarCalculo(conexion.estaConectado());
            mostrarError(mensaje);
        });
    }

    private void habilitarCalculo(final boolean habilitado) {
        distancia.setEnabled(habilitado);
        velocidad.setEnabled(habilitado);
        calcular.setEnabled(habilitado);
    }

    private void mostrarError(final String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Cliente UDP", JOptionPane.WARNING_MESSAGE);
    }
}