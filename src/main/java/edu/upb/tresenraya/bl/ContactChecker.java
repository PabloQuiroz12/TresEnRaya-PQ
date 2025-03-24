package edu.upb.tresenraya.bl;

import edu.upb.tresenraya.network.SocketClient;
import java.util.List;

public class ContactChecker extends Thread {

    private final List<Contacto> contactos;
    private final Runnable onUpdate;

    public ContactChecker(List<Contacto> contactos, Runnable onUpdate) {
        this.contactos = contactos;
        this.onUpdate = onUpdate;
    }

    @Override
    public void run() {
        while (true) {
            boolean huboCambios = false;

            for (Contacto c : contactos) {
                boolean conectado = SocketClient.ping(c.getIp());

                if (c.isStateConnect() != conectado) {
                    c.setStateConnect(conectado);
                    huboCambios = true;
                }
            }

            if (huboCambios && onUpdate != null) {
                onUpdate.run(); // actualiza visualmente la UI
            }

            try {
                Thread.sleep(3000); // Espera 3 segundos antes de volver a comprobar
            } catch (InterruptedException e) {
                break; // Detener el hilo si es interrumpido
            }
        }
    }
}
