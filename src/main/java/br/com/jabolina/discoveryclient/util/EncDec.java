package br.com.jabolina.discoveryclient.util;

import java.util.UUID;

public final class EncDec {

    private EncDec() { }

    public static String jid( String name, String prefix ) {
        return prefix + name + "-" + UUID.randomUUID().toString().split( "-" )[0];
    }
}
