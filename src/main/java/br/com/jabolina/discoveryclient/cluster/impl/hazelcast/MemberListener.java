package br.com.jabolina.discoveryclient.cluster.impl.hazelcast;

import com.hazelcast.core.MemberAttributeEvent;
import com.hazelcast.core.MembershipEvent;
import com.hazelcast.core.MembershipListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MemberListener implements MembershipListener {

    private static final Logger LOGGER = LoggerFactory.getLogger( MembershipListener.class );

    @Override
    public void memberAdded( MembershipEvent membershipEvent ) {
        LOGGER.info( "New member arrived [{}]", membershipEvent );

    }

    @Override
    public void memberRemoved( MembershipEvent membershipEvent ) {
        LOGGER.info( "Member leaving [{}]", membershipEvent );
    }

    @Override
    public void memberAttributeChanged( MemberAttributeEvent memberAttributeEvent ) {
        LOGGER.info( "Member changed [{}]", memberAttributeEvent );
    }
}
