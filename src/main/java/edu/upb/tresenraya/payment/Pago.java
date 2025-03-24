package edu.upb.tresenraya.payment;

public abstract class Pago {

    protected String monto;

    public Pago(String monto) {
        this.monto = monto;
    }

    public abstract void procesarPago();

}
