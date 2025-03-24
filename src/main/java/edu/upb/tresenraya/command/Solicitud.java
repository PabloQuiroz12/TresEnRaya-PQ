package edu.upb.tresenraya.command;

public class Solicitud extends Command{

    public Solicitud() {
        super("0001");
    }

    @Override
    public String parsear(String comando) {
        return comando.substring(5);
    }
    
}
