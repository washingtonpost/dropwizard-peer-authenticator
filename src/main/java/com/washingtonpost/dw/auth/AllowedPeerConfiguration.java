package com.washingtonpost.dw.auth;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.cache.CacheBuilderSpec;
import com.washingtonpost.dw.auth.dao.FlatFilePeerDAO;
import com.washingtonpost.dw.auth.dao.StringPeerDAO;
import com.washingtonpost.dw.auth.model.Peer;
import io.dropwizard.auth.AuthFactory;
import io.dropwizard.auth.Authenticator;
import io.dropwizard.auth.CachingAuthenticator;
import io.dropwizard.auth.basic.BasicAuthFactory;
import io.dropwizard.auth.basic.BasicCredentials;
import io.dropwizard.setup.Environment;
import java.io.InputStream;
import jersey.repackaged.com.google.common.base.Preconditions;

/**
 * <p>Container for configuration, in the "config + factory" pattern that DropWizard likes</p>
 * <p>This configuration can behave in a couple different ways, depending on what properties are set:
 * <ol>
 *   <li>If a "credentialFile" is specified (i.e. non-null), the usernames and passwords of the allowed peers will be read
 * from that file</li>
 *   <li>If instead the "users" and "passwords" strings are specified (i.e. non-null), then those strings are split up
 * with whatever value is specified by the "delimited" property and the token (user, password)s are used as the list of
 * allowed peers.  <br/> By default, the delimiter is ";", so if {@code users="bob;alice"} and {@code passwords="foo;bar"} then
 * the list of allowed peers would contain "user bob with password foo" and "user alice with password bar"</li>
 * </ol>
 * </p>
 * <p>If a cachePolicy is set, then the Authenticator that is registered with Jersey upon calling {@code registerAuthenticator}
 * will be a CachingAuthenticator.  Otherwise, it'll be an instance of {@code AllowedPeerAuthenticator}</p>
 */
public class AllowedPeerConfiguration {

    private String realm = "peers";
    private CacheBuilderSpec cachePolicy;

    private String credentialFile;

    private String users;
    private String passwords;
    private String delimiter = ";";

    /**
     * @return  BasicAuth Realm (name not really important; just needed for response
     * http://tools.ietf.org/html/rfc2617#section-3.2.1)
     */
    @JsonProperty("realm")
    public String getRealm() {
        return realm;
    }

    /**
     * @param realm  BasicAuth Realm (name not really important; just needed for response
     * http://tools.ietf.org/html/rfc2617#section-3.2.1).  If not set, it defaults to "peers"
     */
    @JsonProperty("realm")
    public void setRealm(String realm) {
        this.realm = realm;
    }

    /**
     * @return The classpath-relative name of a properties file holding (user=password) pairs that will count as authorized
     * users of your service.  Example value "peers/allowed-peers.properties"
     */
    @JsonProperty("credentialFile")
    public String getCredentialFile() {
        return credentialFile;
    }

    /**
     * @param credentialFile The classpath-relative name of a properties file holding (user=password) pairs that will count as
     * authorized users of your service.  Example value "peers/allowed-peers.properties".
     */
    @JsonProperty("credentialFile")
    public void setCredentialFile(String credentialFile) {
        this.credentialFile = credentialFile;
    }

    /**
     * @return A String conforming to Guava's CacheBuilderSpec that is used if/when returning a CachingAuthenticator.
     */
    @JsonProperty("cachePolicy")
    public CacheBuilderSpec getCachePolicy() {
        return cachePolicy;
    }

    /**
     * @param cachePolicy A String conforming to Guava's CacheBuilderSpec that is used if/when returning a CachingAuthenticator.
     */
    @JsonProperty("cachePolicy")
    public void setCachePolicy(CacheBuilderSpec cachePolicy) {
        this.cachePolicy = cachePolicy;
    }

    /**
     * @return A delimiter-separated list of users who are authorized peers of your Dropwizard service.
     */
    @JsonProperty("users")
    public String getUsers() {
        return users;
    }

    /**
     * @param users A delimiter-separated list of users who are authorized peers of your Dropwizard service.
     */
    @JsonProperty("users")
    public void setUsers(String users) {
        this.users = users;
    }

    /**
     * @return A delimiter-separated list of passwords associated to the {@code users}.  There must be 1 password for each
     * user
     */
    @JsonProperty("passwords")
    public String getPasswords() {
        return passwords;
    }

    /**
     * @param passwords A delimiter-separated list of passwords associated to the {@code users}.  There must be 1 password for
     * each user
     */
    @JsonProperty("passwords")
    public void setPasswords(String passwords) {
        this.passwords = passwords;
    }

    /**
     * @return The string that separates each user and each password in the {@code users} and {@code passwords} strings.
     * This defaults to the semi-colon (";")
     */
    @JsonProperty("delimiter")
    public String getDelimiter() {
        return delimiter;
    }

    /**
     * @param delimiter The string that separates each user and each password in the {@code users} and {@code passwords}
     * strings. This defaults to the semi-colon (";")
     */
    @JsonProperty("delimiter")
    public void setDelimiter(String delimiter) {
        this.delimiter = delimiter;
    }



    /**
     * <p>If a credentialFile is provided, this method will use that file to populate the list of Peers the Authenticator
     * checks during request processing.  If instead the "users" and "passwords" Strings are provided, this method will use
     * those to populate the list of Peers.</p>
     * @return An Authenticator appropriate for registering with Jersey as described 
     * https://dropwizard.github.io/dropwizard/manual/auth.html
     */
    public Authenticator<BasicCredentials, Peer> createAuthenticator() {
        if (this.credentialFile != null) {
            InputStream allowedPeersResource = this.getClass().getClassLoader().getResourceAsStream(this.credentialFile);
            return new AllowedPeerAuthenticator(new FlatFilePeerDAO(allowedPeersResource));
        }
        else if (this.users != null && this.passwords != null && this.delimiter != null) {
            return new AllowedPeerAuthenticator(new StringPeerDAO(this.users, this.passwords, this.delimiter));
        }
        else {
            throw new IllegalStateException("Illegal call to createAuthenticator() when no valid configuration was set");
        }
    }

    /**
     * @param metrics A metrics registry
     * @return The Authenticator you'd get by calling {@code createAuthenticator} directly, but wrapped in the Dropwizard
     * CachingAuthenticator proxy with this configuration object's {@code cachePolicy} applied to it.
     */
    public CachingAuthenticator<BasicCredentials, Peer> createCachingAuthenticator(MetricRegistry metrics) {
        Preconditions.checkNotNull(this.cachePolicy, "Illegal call to createCachingAuthenticator() when the configuration "
                + "object's cachePolicy attribute is null");
        return new CachingAuthenticator<>(metrics, createAuthenticator(), this.cachePolicy);
    }

    /**
     * This method registers the authenticator configured in this Configuration class with Jersey.
     * @param environment A DropWizard environment
     */
    public void registerAuthenticator(Environment environment) {
        Preconditions.checkNotNull(environment, "Illegal call to registerAuthenticator with a null Environment object");
        Authenticator<BasicCredentials, Peer> authenticator;
        if (this.cachePolicy != null) {
            authenticator = createCachingAuthenticator(environment.metrics());
        }
        else {
            authenticator = createAuthenticator();
        }
        environment.jersey().register(AuthFactory.binder(new BasicAuthFactory<Peer>(authenticator, this.realm, Peer.class)));
    }
}
