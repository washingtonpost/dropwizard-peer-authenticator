package com.washingtonpost.dw.auth.dao;

import com.washingtonpost.dw.auth.model.Peer;
import java.util.Collection;

/**
 * <p>Contract any PeerDAO implementation needs to provide.</p>
 */
public interface PeerDAO {

    /**
     * @return Get all allowed Peers.  See implementation class for any guarantees about the nature of the returned Collection.
     */
    Collection<Peer> findAll();
}
