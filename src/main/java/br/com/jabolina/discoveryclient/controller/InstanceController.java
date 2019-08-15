package br.com.jabolina.discoveryclient.controller;

import br.com.jabolina.discoveryclient.cluster.IDistributedInstance;
import com.hazelcast.core.HazelcastInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping( "/api/instance/" )
public class InstanceController {

    private static final Logger LOGGER = LoggerFactory.getLogger( InstanceController.class );
    private final IDistributedInstance distributedInstance;

    @Autowired
    public InstanceController( IDistributedInstance distributedInstance ) {
        this.distributedInstance = distributedInstance;
    }

    @GetMapping
    public @ResponseBody
    ResponseEntity< Map< String, String > > retrieveInstanceInformation() {
        HazelcastInstance instance = ( HazelcastInstance ) distributedInstance.< HazelcastInstance >retrieveInstance();

        LOGGER.info( "Retrieving information for [{}]", instance );

        return ResponseEntity.ok(
                new HashMap< String, String >() {{
                    put( "leader", String.valueOf( distributedInstance.isLeader() ) );
                    put( "running", String.valueOf( distributedInstance.isRunning() ) );
                    put( "name", instance.getName() );
                }}
        );
    }
}
