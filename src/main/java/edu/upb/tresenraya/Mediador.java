package edu.upb.tresenraya;

import java.util.List;
import java.util.ArrayList;

public class Mediador {

    public static List<MessageListener> listener = new ArrayList<>();

    public Mediador() {
    }

    public static void addListener(MessageListener messageListener) {
        listener.add(messageListener);
    }

    public static void sendMessage(String msg) {
        for (MessageListener messageListener : listener) {
            java.awt.EventQueue.invokeLater(new Runnable() {
                public void run() {
                    messageListener.onMessageReceived(msg);
                }
            });
        }
    }

    public static void sendDuplicateMessage(String msg) {
        for (MessageListener messageListener : listener) {
            java.awt.EventQueue.invokeLater(new Runnable() {
                public void run() {
                    messageListener.onMessageDuplicateRecieved(msg);
                }
            });
        }
    }

    public static void sendIp(String ip) {
        for (MessageListener messageListener : listener) {
            java.awt.EventQueue.invokeLater(new Runnable() {
                public void run() {
                    messageListener.onIpReceived(ip);
                }
            });
        }
    }

    public static void changeTheme(String color) {
        for (MessageListener messageListener : listener) {
            java.awt.EventQueue.invokeLater(new Runnable() {
                public void run() {
                    messageListener.onChangeTheme(color);
                }
            });
        }
    }

    public static void sendInvitation(String nombre, String ip) {
        for (MessageListener messageListener : listener) {
            java.awt.EventQueue.invokeLater(new Runnable() {
                public void run() {
                    messageListener.onSendInvitation(nombre, ip);
                }
            });
        }
    }

    public static void acepting(String nombre) {
        for (MessageListener messageListener : listener) {
            java.awt.EventQueue.invokeLater(new Runnable() {
                public void run() {
                    messageListener.onAcepted(nombre);
                }
            });
        }
    }

    public static void rejecting() {
        for (MessageListener messageListener : listener) {
            java.awt.EventQueue.invokeLater(new Runnable() {
                public void run() {
                    messageListener.onRejected();
                }
            });
        }
    }

    public static void begin(String nombre, String symbol, String ip) {
        for (MessageListener messageListener : listener) {
            java.awt.EventQueue.invokeLater(new Runnable() {
                public void run() {
                    messageListener.onBeginGame(nombre, symbol, ip);
                }
            });
        }
    }

    public static void rejectingGame() {
        for (MessageListener messageListener : listener) {
            java.awt.EventQueue.invokeLater(new Runnable() {
                public void run() {
                    messageListener.onRejectGame();
                }
            });
        }
    }

    public static void aceptingGame() {
        for (MessageListener messageListener : listener) {
            java.awt.EventQueue.invokeLater(new Runnable() {
                public void run() {
                    messageListener.onAcceptGame();
                }
            });
        }
    }

    public static void newGame() {
        for (MessageListener messageListener : listener) {
            java.awt.EventQueue.invokeLater(new Runnable() {
                public void run() {
                    messageListener.onNewGame();
                }
            });
        }
    }

    public static void mark(String symbol) {
        for (MessageListener messageListener : listener) {
            java.awt.EventQueue.invokeLater(new Runnable() {
                public void run() {
                    messageListener.onMarked(symbol);
                }
            });
        }
    }

    public static void removeClient(String nombre) {
        for (MessageListener listener : listener) {
            java.awt.EventQueue.invokeLater(() -> listener.onClienteEliminado(nombre));
        }
    }

    public static void marcarMovimientoExtra(String simbolo, int posX, int posY) {
        for (MessageListener listener : listener) {
            listener.onMovimientoExtra(simbolo + "|" + posX + "|" + posY);
        }
    }
}
