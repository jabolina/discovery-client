package br.com.jabolina.discoveryclient.cluster.leader.service;

import br.com.jabolina.discoveryclient.cluster.IDistributedInstance;
import br.com.jabolina.discoveryclient.data.ServiceDescription;
import br.com.jabolina.discoveryclient.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Objects;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class LeaderKeepAliveService {

    private static final Logger LOGGER = LoggerFactory.getLogger( LeaderKeepAliveService.class );

    private static final Long VERIFY_RATE = 60L;
    private static final Long START_DELAY = 10L;
    private static final String SERVICE_HEALTH_PATH = "/discovery/health";

    private final ScheduledExecutorService executorService;
    private final IDistributedInstance distributedInstance;
    private final RestTemplate restTemplate;

    @Autowired
    public LeaderKeepAliveService(
            IDistributedInstance distributedInstance,
            RestTemplate restTemplate
    ) {
        this.distributedInstance = distributedInstance;
        this.restTemplate = restTemplate;

        this.executorService = Executors.newScheduledThreadPool( 1 );
    }

    private < S extends ServiceDescription > S verifyService( S service ) {
        try {
            ResponseEntity< S > res = restTemplate.exchange(
                    service.getBaseUrl() + SERVICE_HEALTH_PATH,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference< S >() { }
            );

            LOGGER.info( "Service verification response [{}]", res );
            service.setActive( true );
        } catch ( Exception ex ) {
            LOGGER.info( "Deactivating service [{}]", service );
            service.setActive( false );
        }

        return service;
    }

    private <S extends ServiceDescription> void keepAlive() {
        if ( distributedInstance.isLeader() ) {
            distributedInstance.runWithLock( Constants.HAZEL_LOCK_VERIFY, () -> {
                ConcurrentMap< String, S > map = distributedInstance.getMap( Constants.HAZEL_MAP_SERVICES );
                map.values().stream()
                        .map( this::verifyService )
                        .forEach( s -> map.replace( s.getId(), s ) );
            } );
        }
    }

    public void stop() {
        if ( !Objects.isNull( executorService )
                && !executorService.isShutdown()
                && !executorService.isTerminated() ) {

            LOGGER.info( "Canceling leader keep alive service" );
            executorService.shutdownNow();
        }
    }

    public void startServicesVerification() {
        LOGGER.info( "Leader starting services verifications, instance [{}]", distributedInstance );
        executorService.scheduleAtFixedRate(
                this::keepAlive,
                START_DELAY,
                VERIFY_RATE,
                TimeUnit.SECONDS
        );
    }
}
