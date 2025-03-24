package edu.upb.tresenraya.ui;

import edu.upb.tresenraya.MyCollection;

public class Main {

    public static void main(String[] args) {
        MyCollection lista = new MyCollection();
        lista.addItem(1);
        lista.addItem(2);
        lista.addItem(3);
        lista.addItem(4);
        lista.addItem("Hola");
        lista.addItem(6);
        lista.addItem(7);
        while (lista.hasNext()) {
            System.out.println(lista.getNext().toString());
        }

        while (lista.hasPrevious()) {
            System.out.println(lista.getPrevious().toString());
        }
    }
}
