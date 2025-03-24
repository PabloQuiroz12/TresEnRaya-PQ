package edu.upb.tresenraya.command;

public class Marcar extends Command{
    private String simbolo;
    private int posX;
    private int posY;

    public Marcar() {
        super("0008");
    }

    @Override
    public String parsear(String comando) {
        String lol = comando.substring(5);
        String[] partes = lol.split("\\|");
        this.simbolo = partes[0];
        this.posX = Integer.parseInt(partes[1]);
        this.posY = Integer.parseInt(partes[2]);
        System.out.println(simbolo);
        System.out.println(posX);
        System.out.println(posY);
        return simbolo + posX + posY;
        
    }
    
}
