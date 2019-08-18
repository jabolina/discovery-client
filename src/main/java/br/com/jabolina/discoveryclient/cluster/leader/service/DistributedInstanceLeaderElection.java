package br.com.jabolina.discoveryclient.cluster.leader.service;

import br.com.jabolina.discoveryclient.cluster.IDistributedInstance;
import br.com.jabolina.discoveryclient.cluster.leader.event.LeaderElectionEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

@Component
@DependsOn( "custom-distributed-instance" )
public class DistributedInstanceLeaderElection {

    private static final Logger LOGGER = LoggerFactory.getLogger( DistributedInstanceLeaderElection.class );

    private final IDistributedInstance distributedInstance;
    private final LeaderElectionEventPublisher eventPublisher;

    private Lock leaderLock;

    private final Object masterLock = new Object();
    private final Object starterLock = new Object();

    private LeaderElectionThread electionThread = new LeaderElectionThread();

    private boolean isRunning = true;
    private boolean isStarting = false;

    @Autowired
    public DistributedInstanceLeaderElection(
            IDistributedInstance distributedInstance,
            LeaderElectionEventPublisher eventPublisher
    ) {
        this.distributedInstance = distributedInstance;
        this.eventPublisher = eventPublisher;
    }

    @PostConstruct
    public void candidate() {
        leaderLock = distributedInstance.getLock( "leadership-election-lock" );
        electionThread.start();
        Runtime.getRuntime().addShutdownHook( new Thread( this::shutdown ) );
    }

    public boolean isMaster() {
        synchronized ( starterLock ) {
            if ( !isStarting ) {
                try {
                    starterLock.wait();
                } catch ( InterruptedException ignore ) {
                }
            }
        }

        return electionThread.isMaster();
    }

    @PreDestroy
    public void shutdown() {
        if ( !isRunning ) {
            return;
        }

        isRunning = false;

        try {
            synchronized ( masterLock ) {
                masterLock.notifyAll();
            }

            electionThread.join();
        } catch ( InterruptedException ignore ) {
        } finally {
            LOGGER.info( "Leader giving up" );
            eventPublisher.publishNewElectedLeader( null );
        }
    }

    private class LeaderElectionThread extends Thread {
        private boolean isMaster = false;

        @Override
        public void run() {
            while ( isRunning ) {
                try {
                    if ( isMaster ) {
                        synchronized ( masterLock ) {
                            if ( isStarting ) {
                                masterLock.wait();
                            } else {
                                masterLock.wait( Duration.ofSeconds( 5L ).toMillis() );
                            }
                        }
                    } else if ( distributedInstance.isRunning() ) {
                        isMaster = leaderLock.tryLock( 5L, TimeUnit.SECONDS );
                        if ( isMaster ) {
                            LOGGER.info( "New leader elected!" );
                            eventPublisher.publishNewElectedLeader( distributedInstance );
                        }
                    }
                } catch ( InterruptedException ignore ) {
                } finally {
                    synchronized ( starterLock ) {
                        if ( !isStarting ) {
                            starterLock.notifyAll();
                            isStarting = true;
                        }
                    }
                }
            }

            if ( isMaster ) {
                leaderLock.unlock();
                isMaster = false;
            }
        }

        public boolean isMaster() {
            return isMaster;
        }
    }
}
