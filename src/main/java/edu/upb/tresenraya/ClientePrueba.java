package edu.upb.tresenraya;

import java.io.DataOutputStream;
import java.net.Socket;
import java.util.UUID;

public class ClientePrueba {

    public static void main(String[] arg) {
        try {
            Socket socket = new Socket("172.16.41.87", 1825);
            System.out.println("Cliente conectado...");


            DataOutputStream dout = new DataOutputStream(socket.getOutputStream());

            UUID uuidPersona = UUID.randomUUID();
            UUID uuidMensaje = UUID.randomUUID();

            String menss = "Hola Mundo";

           String mensaje = "0008|O|2|2"+System.lineSeparator();

            System.out.println(mensaje);
            dout.write(mensaje.getBytes("UTF-8"));
            dout.flush();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
