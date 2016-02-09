package com.washingtonpost.dw.auth.dao;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import com.washingtonpost.dw.auth.encryptor.JasyptEncryptor;
import com.washingtonpost.dw.auth.model.Peer;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>Cheezy/temporary implementation that just reads in hard-coded allowed Peers from a flat file on our classpath</p>
 * <p>We'll want to figure out how to manage allowed-peer relationships in the future.  One obvious place is just to store
 * them in the Admiral database that this service already connects to, but that doesn't sit 100% well with me because Admiral
 * owns the Admiral schema and doesn't know/care about this arc-auth service, so it's odd that it'd be handling the migration
 * of a table designed only for this service.</p>
 */
public class FlatFilePeerDAO implements PeerDAO {

    private static final Logger LOGGER = LoggerFactory.getLogger(FlatFilePeerDAO.class);
    private final Properties allowedPeers;

    /**
     * @param inputStream An inputs stream holding a bunch of "user=password" key/pairs in Java Properties format.  This
     * constructor will close the InputStream after reading Peers from the stream.
     */
    public FlatFilePeerDAO(InputStream inputStream) {
        this.allowedPeers = new Properties();
        try {
            this.allowedPeers.load(inputStream);
            inputStream.close();
        }
        catch (IOException ioe) {
            LOGGER.error("Could not load allowed peers into a Properties object", ioe);
            throw new RuntimeException("This application requires a classpath-accessible file configured under the "
                    + "allowedPeers.credentialFile property to load the allowed requestors of this service.  No such file "
                    + "could be loaded.", ioe);
        }
    }

    /**
     * @return A Collection of all the allowed Peers.  Note that while the source data may include encrypted passwords
     * wrapped in an "ENC(...)" string to indicate to humans that the password is encrypted, this implementation strips off
     * that ENC(...) wrapper to enable a simplify password-comparison logic.
     */
    @Override
    public Set<Peer> findAll() {
        Set<Peer> peers = Sets.newLinkedHashSetWithExpectedSize(this.allowedPeers.size());
        allowedPeers.entrySet().stream().forEach((entrySet) -> {
            String username = (String)entrySet.getKey();
            Preconditions.checkState(nameIsUnique(peers, username), "Can't have 2 identical usernames");

            String password = JasyptEncryptor.getEncryptedPart((String)entrySet.getValue());

            peers.add(new Peer(username, password));
        });
        return peers;
    }

}
