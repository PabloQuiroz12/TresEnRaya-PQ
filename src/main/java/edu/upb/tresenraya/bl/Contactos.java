package edu.upb.tresenraya.bl;

import edu.upb.tresenraya.network.SocketClient;
import edu.upb.tresenraya.network.SocketListener;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class Contactos implements SocketListener {

    private final Map<String, SocketClient> contactos = new HashMap<>();
    private static final Contactos instance = new Contactos();
    private final List<Runnable> listeners = new ArrayList<>();

    private Contactos() {
        System.out.println("Inicializando Contactos...");
        MediadorContactos.getInstance().addListener(this); // ← Se conecta al mediador
    }

    public static Contactos getInstance() {
        return instance;
    }

    public List<Contacto> getListaContactos() {
        List<Contacto> lista = new ArrayList<>();
        for (String ip : contactos.keySet()) {
            SocketClient sc = contactos.get(ip);
            lista.add(new Contacto(sc.getIp(), ip, true));
        }
        return lista;
    }

    @Override
    public void onNewClient(SocketClient sc) {
        if (sc == null) {
            System.err.println("Error: Se intentó agregar un cliente nulo.");
            return;
        }

        synchronized (contactos) {
            if (!contactos.containsKey(sc.getIp())) {
                contactos.put(sc.getIp(), sc);
            }
        }

        System.out.println("Total de contactos conectados: " + contactos.size());
        notifyListeners();
    }

    public void send(String ip, String msg) {
        System.out.println("Enviando mensaje a " + ip + ": " + msg);
        SocketClient sc = this.contactos.get(ip);
        if (sc != null) {
            sc.send(msg.getBytes());
        }
    }

    public void addListener(Runnable listener) {
        listeners.add(listener);
    }

    private void notifyListeners() {
        for (Runnable listener : listeners) {
            listener.run();
        }
    }

    public void eliminarContacto(String nombre) {
        synchronized (contactos) {
            SocketClient eliminado = null;
            for (Map.Entry<String, SocketClient> entry : contactos.entrySet()) {
                if (entry.getValue().getIp().equals(nombre)) {
                    eliminado = entry.getValue();
                    break;
                }
            }
            if (eliminado != null) {
                contactos.remove(eliminado.getIp());
                System.out.println("❌ Cliente eliminado: " + nombre);
                notifyListeners();
            }
        }
    }

   

}
