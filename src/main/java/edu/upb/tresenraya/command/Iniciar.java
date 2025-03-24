package edu.upb.tresenraya.command;

public class Iniciar extends Command{

    public Iniciar() {
        super("0004");
    }

    @Override
    public String parsear(String comando) {
        return comando.substring(5);
    }
    
}
