package edu.upb.tresenraya.bl;

import edu.upb.tresenraya.MyCollection;
import edu.upb.tresenraya.network.SocketClient;
import edu.upb.tresenraya.network.SocketListener;

public class MediadorContactos {

    private final MyCollection<SocketListener> listeners = new MyCollection<>();
    private static final MediadorContactos instance = new MediadorContactos();

    private MediadorContactos() {
    }

    public static MediadorContactos getInstance() { // ← Corregido el nombre del método
        return instance;
    }

    public void addListener(SocketListener sl) {
        listeners.addItem(sl);
        System.out.println("Listener agregado: " + sl.getClass().getSimpleName());
    }

    public void newClient(SocketClient client) {
        if (client == null) {
            System.err.println("Error: Intento de agregar un cliente nulo en MediadorContactos.");
            return;
        }

        System.out.println("Nuevo cliente detectado: " + client.getIp());
        listeners.resetIterator();

        while (listeners.hasNext()) {
            SocketListener listener = listeners.getNext();
            java.awt.EventQueue.invokeLater(() -> {
                System.out.println("Notificando a listener: " + listener.getClass().getSimpleName());
                listener.onNewClient(client);
            });
        }
    }
}
