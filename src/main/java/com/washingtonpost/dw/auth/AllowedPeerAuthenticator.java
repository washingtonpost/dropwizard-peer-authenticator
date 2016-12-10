package com.washingtonpost.dw.auth;

import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableSet;
import com.washingtonpost.dw.auth.dao.PeerDAO;
import com.washingtonpost.dw.auth.model.Peer;
import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;
import io.dropwizard.auth.basic.BasicCredentials;
import java.util.Optional;
import java.util.Set;
import org.jasypt.util.password.PasswordEncryptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * <p>Implementation of a DropWizard Authenticator interface that forces the our callers to authenticate with us via Basic
 * Auth.</p>
 */
public class AllowedPeerAuthenticator implements Authenticator<BasicCredentials, Peer> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AllowedPeerAuthenticator.class);
    private final Set<Peer> allPeers;
    private final PasswordEncryptor passwordEncryptor;

    public AllowedPeerAuthenticator(PeerDAO peerDAO, PasswordEncryptor passwordEncryptor) {
        this.allPeers = peerDAO.findAll();
        this.passwordEncryptor = passwordEncryptor;
        LOGGER.info("Constructed Authenticator with {} allowed peers", this.allPeers.size());
    }


    @Override
    public Optional<Peer> authenticate(BasicCredentials credentials) throws AuthenticationException {
        if (this.passwordEncryptor == null) {
            return authenticateUnencrypted(credentials);
        }
        else {
            return authenticateEncrypted(credentials);
        }
    }

    /*
    If we're configured with a null {@code PasswordEncryptor}, assume our allowed-peers.properties contains a bunch of
    plaintext passwords, so just do a normal .equals comparison between {@code credentials} and {@code allPeers}
    */
    private Optional<Peer> authenticateUnencrypted(BasicCredentials credentials) throws AuthenticationException {
        Peer peer = new Peer(credentials.getUsername(), credentials.getPassword());

        if (this.allPeers.contains(peer)) {
            LOGGER.debug("{} authenticated and allowed to request service", credentials.getUsername());
            return Optional.of(peer);
        }
        else {
            LOGGER.debug("{} is not known in our list of allowed peers", credentials.getUsername());
        }
        return Optional.empty();
    }

    /*
    If we're configured with a non-null {@code PasswordEncryptor}, use the `checkPassword` function to make sure the
    encrypted version of {@code credentials.getPassword()} matches the assumed-encrypted property in our
    allowed-peers.properties file
    */
    private Optional<Peer> authenticateEncrypted(BasicCredentials credentials) {
        Set<Peer> peers = ImmutableSet.<Peer>copyOf(
            Collections2.filter(allPeers, (Peer p) -> p.getName().equals(credentials.getUsername())));

        if (peers.isEmpty()) {
            LOGGER.debug("No peer named {} found in our allowed-peers file", credentials.getUsername());
            return Optional.empty();
        }
        else {
            Peer peer = peers.stream().findFirst().get();
            if (this.passwordEncryptor.checkPassword(credentials.getPassword(), peer.getPassword())) {
                LOGGER.debug("{} authenticated and allowed to request service", credentials.getUsername());
                return Optional.of(peer);
            }
            else {
                LOGGER.debug("{} is not known in our list of allowed peers", credentials.getUsername());
                return Optional.empty();
            }
        }
    }
}
