package edu.upb.tresenraya.payment;

public class PagoFactory {

    public static Pago crearPago(String tipo, String monto, String numeroTarjeta, String pin) {
        if (tipo.equalsIgnoreCase("Tarjeta")) {
            return new PagoTarjeta(monto, numeroTarjeta, pin);
        } else if (tipo.equalsIgnoreCase("QR")) {
            return new PagoQR(monto);
        } else {
            throw new IllegalArgumentException("Metodo de pago no válido");
        }
    }
}
