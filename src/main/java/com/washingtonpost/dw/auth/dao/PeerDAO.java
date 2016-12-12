package com.washingtonpost.dw.auth.dao;

import com.washingtonpost.dw.auth.model.Peer;
import java.util.Set;

/**
 * <p>
 * Contract any PeerDAO implementation needs to provide.</p>
 */
public interface PeerDAO {

    /**
     * @return Get all allowed Peers. See implementation class for any guarantees about the nature of the returned Collection.
     */
    Set<Peer> findAll();

    /**
     *
     * @param peers A Set of Peers to check to see if there's already someone named {@code username} in it
     * @param username A username to check for existence in {@code peers}
     * @return True, if {@code username} does not appear in {@code peers}, false otherwise
     */
    default boolean nameIsUnique(Set<Peer> peers, String username) {
        return peers.stream().noneMatch((peer) -> (peer.getName().equals(username)));
    }
}
