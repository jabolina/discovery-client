package br.com.jabolina.discoveryclient.util;

import javax.validation.constraints.NotNull;
import java.util.Base64;
import java.util.UUID;

public final class EncDec {

    private EncDec() { }

    public static String jid( String name, String prefix ) {
        return prefix + name + "-" + UUID.randomUUID().toString().split( "-" )[0];
    }

    public static String encode( @NotNull String s ) {
        return Base64.getEncoder().encodeToString( s.getBytes() );
    }
}
