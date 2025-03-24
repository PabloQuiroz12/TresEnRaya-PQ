package edu.upb.tresenraya;

public interface Iterator<T> {

    boolean hasNext();

    boolean hasPrevious();

    void resetIterator();

    T getPrevious();

    T getNext();
}
