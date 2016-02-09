package com.washingtonpost.dw.auth;

import com.codahale.metrics.MetricRegistry;
import com.google.common.cache.CacheBuilderSpec;
import com.washingtonpost.dw.auth.model.Peer;
import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;
import io.dropwizard.auth.CachingAuthenticator;
import io.dropwizard.auth.basic.BasicCredentials;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 * <p>Tests the AllowedPeerConfiguration</p>
 */
public class TestAllowedPeerConfiguration {

    @Test
    public void testCreateAuthenticatorWithCredentialFile() throws AuthenticationException {
        AllowedPeerConfiguration config = new AllowedPeerConfiguration();
        config.setCredentialFile("peers/test-peers.properties");

        Authenticator<BasicCredentials, Peer> authenticator = config.createAuthenticator();
        assertTrue(authenticator.authenticate(new BasicCredentials("foo", "bar")).isPresent());
        assertFalse(authenticator.authenticate(new BasicCredentials("not in", "our test properties")).isPresent());
    }

    @Test
    public void testCreateAuthenticatorWithBasicEncryptedCredentialFile() throws AuthenticationException {
        AllowedPeerConfiguration config = new AllowedPeerConfiguration();
        config.setCredentialFile("peers/test-peers-encrypted-basic.properties");
        config.setEncryptor(AllowedPeerConfiguration.Encryptor.BASIC);

        Authenticator<BasicCredentials, Peer> authenticator = config.createAuthenticator();
        assertTrue(authenticator.authenticate(new BasicCredentials("foo", "bar")).isPresent());
        assertFalse(authenticator.authenticate(new BasicCredentials("not in", "our test properties")).isPresent());
        assertFalse(authenticator.authenticate(new BasicCredentials("foo", "wrong password")).isPresent());
    }

    @Test
    public void testCreateAuthenticatorWithStrings() throws AuthenticationException {
        AllowedPeerConfiguration config = new AllowedPeerConfiguration();
        config.setUsers("bob;alice");
        config.setPasswords("secret;1234");

        Authenticator<BasicCredentials, Peer> authenticator = config.createAuthenticator();
        assertTrue(authenticator.authenticate(new BasicCredentials("bob", "secret")).isPresent());
        assertTrue(authenticator.authenticate(new BasicCredentials("alice", "1234")).isPresent());
        assertFalse(authenticator.authenticate(new BasicCredentials("not in", "our strings")).isPresent());
    }

    @Test(expected=IllegalStateException.class)
    public void testCreateAuthentorWithBadConfiguration() {
        AllowedPeerConfiguration config = new AllowedPeerConfiguration();
        config.createAuthenticator();
    }

    @Test
    public void testCreateCachingAuthentiator() throws AuthenticationException {
        AllowedPeerConfiguration config = new AllowedPeerConfiguration();
        config.setCredentialFile("peers/test-peers.properties");
        config.setCachePolicy(CacheBuilderSpec.parse("maximumSize=100, expireAfterAccess=10m"));

        CachingAuthenticator<BasicCredentials, Peer> cachingAuthenticator =
                config.createCachingAuthenticator(new MetricRegistry());
        assertTrue(cachingAuthenticator.authenticate(new BasicCredentials("foo", "bar")).isPresent());
    }


    @Test(expected=NullPointerException.class)
    public void testCreateCachingAuthenticatorWithNoCachePolicy() {
        AllowedPeerConfiguration config = new AllowedPeerConfiguration();
        config.setCredentialFile("peers/test-peers.properties");

        config.createCachingAuthenticator(new MetricRegistry());
    }
}
