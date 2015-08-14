package com.washingtonpost.dw.auth.dao;

import com.washingtonpost.dw.auth.model.Peer;
import java.util.Collection;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 * <p>Tests the StringPeerDAO</p>
 */
public class TestStringPeerDAO {

    private static final String USERS="bob;alice;zach;nancy;frank";
    private static final String PASSWORDS="1;45238q*&;;SFH(@*FH@*(@HF@F; ";

    @Test
    public void testHappyPath() {
        PeerDAO dao = new StringPeerDAO(USERS, PASSWORDS);
        Collection<Peer> peers = dao.findAll();

        assertTrue(peers.contains(new Peer("bob", "1")));
        assertTrue(peers.contains(new Peer("alice","45238q*&")));
        assertTrue(peers.contains(new Peer("zach", "")));
        assertTrue(peers.contains(new Peer("nancy", "SFH(@*FH@*(@HF@F")));
        assertTrue(peers.contains(new Peer("frank", " ")));
    }
}
