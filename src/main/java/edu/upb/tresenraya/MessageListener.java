package edu.upb.tresenraya;

public interface MessageListener {
    void onMessageReceived(String message);

    void onMessageDuplicateRecieved(String message);

    void onIpReceived(String ip);

    void onChangeTheme(String color);

    void onSendInvitation(String nombre, String ip);

    void onAcepted(String nombre);

    void onRejected();

    void onMarked(String symbol);

    void onBeginGame(String nombre, String symbol, String ip);
    void onRejectGame();
    void onAcceptGame();
    void onNewGame();
    void onClienteEliminado(String nombre);
    void onMovimientoExtra(String message);

}
