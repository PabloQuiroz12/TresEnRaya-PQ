package edu.upb.tresenraya.command;

public abstract class Command {
    protected String comando;

    public Command(String comando) {
        this.comando = comando;
    }

    public String getComando() {
        return comando;
    }

    public abstract String parsear(String comando);
}

