package br.com.jabolina.discoveryclient.cluster.impl;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.MemberAttributeEvent;
import com.hazelcast.core.MembershipEvent;
import com.hazelcast.core.MembershipListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class MemberListener implements MembershipListener {

    private static final Logger LOGGER = LoggerFactory.getLogger( MembershipListener.class );

    @Autowired
    private HazelcastInstance instance;

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
