package com.washingtonpost.dw.auth.dao;

import com.washingtonpost.dw.auth.model.Peer;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import org.junit.Test;
import static org.junit.Assert.assertTrue;

/**
 * <p>Tests the FlatFilePeerDAO</p>
 */
public class TestFlatFilePeerDAO {

    @Test
    public void testFetchAll() throws IOException {
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("peers/test-peers.properties");

        PeerDAO dao = new FlatFilePeerDAO(inputStream);
        Collection<Peer> allPeers = dao.findAll();
        assertTrue(allPeers.size() == 2);
        assertTrue(allPeers.contains(new Peer("testuser", "testpass")));
    }
}
