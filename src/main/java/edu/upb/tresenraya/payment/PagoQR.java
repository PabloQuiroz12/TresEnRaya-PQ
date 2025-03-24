package edu.upb.tresenraya.payment;

public class PagoQR extends Pago {

    public PagoQR(String monto) {
        super(monto);
    }

    @Override
    public void procesarPago() {
        System.out.println("Pago con QR procesado:");
        System.out.println("Monto: " + monto);
    }
}
