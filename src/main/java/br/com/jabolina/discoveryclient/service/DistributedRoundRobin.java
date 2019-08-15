package br.com.jabolina.discoveryclient.service;

import java.util.List;

public interface DistributedRoundRobin {
     < E > E next( List< E > list, String name );
}
