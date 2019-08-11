package br.com.jabolina.discoveryclient.service;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class RoundRobin {

    private RoundRobin() { }

    public static <T> Iterator<T> toIterator( List<T> list ) {
        return new Iterator< T >() {
            private List< T > mem = list;
            private int idx = 0;

            public void add( T t ) {
                mem.add( t );
                Collections.shuffle( mem );
                idx = 0;
            }

            @Override
            public boolean hasNext() {
                return true;
            }

            @Override
            public T next() {
                T t = mem.get( idx );
                idx = ( idx + 1 ) % mem.size();

                return t;
            }

            @Override
            public void remove() { }
        };
    }
}
