package edu.upb.tresenraya.network;

import edu.upb.tresenraya.ui.TresEnRayaUI;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServidorJuego extends Thread {

    private final ServerSocket serverSocket;
    private final TresEnRayaUI ui;

    public ServidorJuego(TresEnRayaUI ui) throws IOException {
        this.serverSocket = new ServerSocket(1825);
        this.ui = ui;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Socket socket = this.serverSocket.accept();
                SocketClient client = new SocketClient(socket);
                client.start();
            } catch (IOException e) {

            }

        }

    }

}
