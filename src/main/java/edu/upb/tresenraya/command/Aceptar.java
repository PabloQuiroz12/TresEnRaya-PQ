package edu.upb.tresenraya.command;

public class Aceptar extends Command{

    public Aceptar() {
        super("0003");
    }

    @Override
    public String parsear(String comando) {
        System.out.println(comando);
        return comando.substring(5);
    }   
}
