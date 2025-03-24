package edu.upb.tresenraya;

import java.util.ArrayList;

public class MyCollection<T> implements Iterator {

    private int index;
    private ArrayList<T> lista = new ArrayList<T>();

    public void addItem(T contact) {
        lista.add(contact);
    }

    @Override
    public boolean hasNext() {
        return index < lista.size();
    }

    @Override
    public T getNext() {
        if (!hasNext()) {
            return null;
        }
        T item = lista.get(index);
        index++;
        return item;
    }

    @Override
    public boolean hasPrevious() {
        return index > 0;
    }

    @Override
    public T getPrevious() {
        if (!hasPrevious()) {
            return null;
        }
        if (index >= lista.size()) {
            index = lista.size() - 1;
        } else {
            index--;
        }
        return lista.get(index);
    }

    @Override
    public void resetIterator() {
        index = 0;
    }
}
