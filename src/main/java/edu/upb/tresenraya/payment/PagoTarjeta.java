package edu.upb.tresenraya.payment;

public class PagoTarjeta extends Pago {

    private String numeroTarjeta;
    private String pin;

    public PagoTarjeta(String monto, String numeroTarjeta, String pin) {
        super(monto);
        this.numeroTarjeta = numeroTarjeta;
        this.pin = pin;
    }

    @Override
    public void procesarPago() {
        System.out.println("Pago con tarjeta procesado:");
        System.out.println("Monto: " + monto);
        System.out.println("Numero de tarjeta: " + numeroTarjeta);
        System.out.println("PIN: " + pin);
    }
}
