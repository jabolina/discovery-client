package br.com.jabolina.discoveryclient.cluster.leader;

import br.com.jabolina.discoveryclient.cluster.IDistributedInstance;
import br.com.jabolina.discoveryclient.cluster.leader.event.LeaderElectionEvent;
import br.com.jabolina.discoveryclient.cluster.leader.service.LeaderKeepAliveService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class LeaderElectionEventListener implements ApplicationListener< LeaderElectionEvent > {

    private static final Logger LOGGER = LoggerFactory.getLogger( LeaderElectionEventListener.class );
    private final LeaderKeepAliveService keepAliveService;

    @Autowired
    public LeaderElectionEventListener(
            LeaderKeepAliveService keepAliveService
    ) {
        this.keepAliveService = keepAliveService;
    }

    private void leaderRevoked() {
        LOGGER.info( "Leader gave up");
        keepAliveService.stop();
    }

    @Override
    public void onApplicationEvent( LeaderElectionEvent event) {
        IDistributedInstance instance = event.getElectedLeader();

        if ( !Objects.isNull( instance ) ) {
            LOGGER.info( "New instance leader elected: [{}]", event );
            instance.elected();
            keepAliveService.startServicesVerification();
        } else {
            leaderRevoked();
        }
    }
}
