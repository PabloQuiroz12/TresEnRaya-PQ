package edu.upb.tresenraya.network;

import edu.upb.tresenraya.Mediador;
import edu.upb.tresenraya.MessageListener;
import edu.upb.tresenraya.bl.Contactos;
import edu.upb.tresenraya.command.Aceptar;
import edu.upb.tresenraya.command.AceptarJuego;
import edu.upb.tresenraya.command.Command;
import edu.upb.tresenraya.command.Iniciar;
import edu.upb.tresenraya.command.Marcar;
import edu.upb.tresenraya.command.NuevaPartida;
import edu.upb.tresenraya.command.Rechazar;
import edu.upb.tresenraya.command.RechazarJuego;
import edu.upb.tresenraya.command.Solicitud;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Scanner;
import lombok.Getter;

@Getter
public class SocketClient extends Thread {

    private static final String C_SOLICITUD = "0001";
    private static final String C_RECHAZAR = "0002";
    private static final String C_ACEPTAR = "0003";
    private static final String C_INICIAR = "0004";
    private static final String C_RECHAZAR_JUEGO = "0005";
    private static final String C_ACEPTAR_JUEGO = "0006";
    private static final String C_NUEVA_PARTIDA = "0007";
    private static final String C_MARCAR = "0008";
    private static final String C_SALIR = "0010";
    private static final String C_MOVIMIENTO_EXTRA = "0011";

    private final Socket socket;
    private String nombre;
    private final String ip;
    private final DataOutputStream dout;
    private final BufferedReader br;
    private MessageListener messageListener;

    public void setMessageListener(MessageListener listener) {
        this.messageListener = listener;
    }

    public SocketClient(Socket socket) throws IOException {
        this.socket = socket;
        this.ip = socket.getInetAddress().getHostAddress();
        dout = new DataOutputStream(socket.getOutputStream());
        br = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
        this.nombre = "Desconocido";
    }

    public String getIp() {
        return ip;
    }

    public static boolean ping(String ip) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(ip, 1825), 1000); // timeout 1 segundo
            return true; // Hay conexión
        } catch (IOException e) {
            return false; // No hay conexión
        }
    }

    @Override
    public void run() {
        try {
            String message;
            while ((message = br.readLine()) != null) {
                System.out.println("Comando: " + message);
                String tipoMensaje = message.substring(0, 4);
                String command;
                Command comando = null;

                if (message.startsWith("MSG:")) {
                    String mensajeLimpio = message.substring(4);
                    System.out.println("Mensaje recibido: " + mensajeLimpio);
                    Mediador.sendMessage(mensajeLimpio);
//                Mediador.sendIp(ip);
                } else if (message.startsWith("CLR:")) {
                    String color = message.substring(4);
                    System.out.println("Cambiando tema a: " + color);
                    Mediador.changeTheme(color);
                } else if (message.startsWith("DUP:")) {
                    String duplicado = message.substring(4);
                    System.out.println("Mensaje recibido: " + duplicado);
                    Mediador.sendDuplicateMessage(duplicado);
                } else {

                    switch (tipoMensaje) {
                        case C_SOLICITUD -> {
                            comando = new Solicitud();
                            String nombreRecibido = comando.parsear(message);
                            this.nombre = nombreRecibido;
                            Mediador.sendInvitation(nombreRecibido, ip);
                        }
                        case C_RECHAZAR -> {
                            comando = new Rechazar();
                            Mediador.rejecting();
                        }
                        case C_ACEPTAR -> {
                            comando = new Aceptar();
                            String nombreRecibido = comando.parsear(message);
                            this.nombre = nombreRecibido;
                            Mediador.acepting(nombreRecibido);;
                        }
                        case C_INICIAR -> {
                            comando = new Iniciar();
                            System.out.println("Jugador conectado: " + this.nombre + " (IP: " + this.ip + ")");
                            Mediador.begin("Jugador: " + this.nombre, comando.parsear(message), ip);
                        }
                        case C_RECHAZAR_JUEGO -> {
                            comando = new RechazarJuego();
                            Mediador.rejectingGame();
                        }
                        case C_ACEPTAR_JUEGO -> {
                            comando = new AceptarJuego();
                            Mediador.aceptingGame();
                        }
                        case C_NUEVA_PARTIDA -> {
                            comando = new NuevaPartida();
                            Mediador.newGame();
                        }
                        case C_MARCAR -> {
                            comando = new Marcar();
                            Mediador.mark(comando.parsear(message));
                        }
                        case C_SALIR -> {
                            String[] partes = message.split("\\|", 2);
                            if (partes.length < 2) {
                                System.out.println("❌ Error en formato de mensaje de salida");
                                return;
                            }

                            String nombreCliente = partes[1];
                            System.out.println("El jugador '" + nombreCliente + "' ha salido del juego");

                            Contactos contactos = Contactos.getInstance();
                            contactos.eliminarContacto(nombreCliente);
                            Mediador.removeClient(nombreCliente);

                            cerrarConexion();
                        }
                        case C_MOVIMIENTO_EXTRA -> {
                            String[] partes = message.split("\\|");
                            if (partes.length != 4) {
                                System.out.println("Error en formato de movimiento extra");
                                return;
                            }
                            String simbolo = partes[1];
                            int posX = Integer.parseInt(partes[2]);
                            int posY = Integer.parseInt(partes[3]);

                            System.out.println("Movimiento extra realizado por " + this.nombre + " en (" + posX + ", " + posY + ")");
                            Mediador.marcarMovimientoExtra(simbolo, posX, posY);
                        }
                        default -> {
                            System.out.println("Comando no reconocido: " + tipoMensaje);
                        }
                    }
                }
            }
            // BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            // send((br.readLine() + System.lineSeparator()).getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void send(byte[] buffer) {
        try {
            dout.write(buffer);
            dout.flush();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void cerrarConexion() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            SocketClient socketClient = new SocketClient(new Socket("localhost", 1825));
            socketClient.start();
            Scanner scanner = new Scanner(System.in);

            while (true) {
                System.out.println("\n Seleccione una opción:");
                System.out.println("1. Enviar mensaje");
                System.out.println("2. Cambiar color");
                System.out.println("3. Enviar mensaje duplicado");
                System.out.println("4. Enviar invitación");
                System.out.println("5. Aceptar invitación");
                System.out.println("6. Rechazar invitación");
                System.out.println("7. Marcar casilla");
                System.out.println("8. Enviar comando '0010' (Eliminar Cliente)");
                System.out.println("9. Enviar comando '0011' (Movimiento Extra)");
                System.out.println("0. Salir");
                System.out.print("Opción: ");
                String option = scanner.nextLine().trim();

                String message = null;

                switch (option) {
                    case "1":
                        System.out.print("Escriba su mensaje: ");
                        message = "MSG:" + scanner.nextLine();
                        break;
                    case "2":
                        System.out.print("Ingrese el color: ");
                        message = "CLR:" + scanner.nextLine();
                        break;
                    case "3":
                        System.out.print("Escriba su mensaje duplicado: ");
                        message = "DUP:" + scanner.nextLine();
                        break;
                    case "4":
                        System.out.print("Ingrese su nombre para la invitación: ");
                        String nombreInvitacion = scanner.nextLine();
                        message = "0001|" + nombreInvitacion;
                        break;
                    case "5":
                        System.out.print("Ingrese el nombre del jugador a aceptar: ");
                        String nombreAceptar = scanner.nextLine();
                        message = "0003|" + nombreAceptar;
                        break;
                    case "6":
                        message = "0002";  // Rechazar invitación
                        break;
                    case "7":
                        System.out.print("Ingrese coordenadas (Ejemplo: 0|0): ");
                        String coordenadas = scanner.nextLine();
                        System.out.print("Ingrese el símbolo (X u O): ");
                        String simbolo = scanner.nextLine().toUpperCase();
                        message = "0008|" + simbolo + "|" + coordenadas;
                        break;
                    case "8":
                        System.out.print("Ingrese su nombre para salir del juego: ");
                        String nombreSalida = scanner.nextLine();
                        message = "0010|" + nombreSalida;
                        break;
                    case "9":
                        System.out.print("Ingrese el símbolo (X u O) para movimiento extra: ");
                        String simboloExtra = scanner.nextLine().toUpperCase();
                        System.out.print("Ingrese coordenadas (Ejemplo: 1|1): ");
                        String coordenadasExtra = scanner.nextLine();
                        message = "0011|" + simboloExtra + "|" + coordenadasExtra;
                        break;
                    case "0":
                        System.out.println("Saliendo del programa...");
                        scanner.close();
                        return;
                    default:
                        System.out.println("Opción inválida. Intente de nuevo.");
                        continue;
                }

                if (message != null) {
                    socketClient.send((message + System.lineSeparator()).getBytes());
                    System.out.println("Comando enviado: " + message);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
