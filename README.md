# dropwizard-peer-authenticator
Dropwizard module to enable BasicAuth security around a service with convenience factories for reading in lists of (users, passwords) who are authorized to make requests of your service.

This artifact provides a Dropwizard Configuration/Factory class that enables convenient registration of an authorization filter with a Dropwizard Jersey Server.  This artifact is essentially a configuration wrapper around the documentation provided at https://dropwizard.github.io/dropwizard/manual/auth.html

A "Peer" object is a (username, password) POJO that models a remote service or user invoking some endpoint in your Dropwizard service.  The AllowedPeerAuthenticator loads a list of allowed peers from some source and then registers itself with Jersey to provide endpoint-level authentication on top of HTTP BasicAuth.

## Example configuration : peer file

Add a file like "allowed-peers.properties" to your classpath with a list of users and passwords that should be allowed to invoke endpoints on your service.  For example:
```yaml
alice=abc123
bob=supersecret
```

In your application configuration YAML, add the configuration:
```
allowedPeers: 
    credentialFile: allowed-peers.properties
```

In your application's Configuration class, add the AllowedPeerConfiguration object like:
```java

import io.dropwizard.Configuration;
import com.washingtonpost.dw.auth.AllowedPeerConfiguration;

public class MyAppConfiguration extends Configuration {

    private AllowedPeerConfiguration allowedPeers = new AllowedPeerConfiguration();

    @JsonProperty("allowedPeers")
    public AllowedPeerConfiguration getAllowedPeers() {
        return this.allowedPeers;
    }

    @JsonProperty("allowedPeers")
    public void setAllowedPeers(AllowedPeerConfiguration allowedPeers) {
        this.allowedPeers = allowedPeers;
    }
```

In your main application class, register the authenticator with jersey:
```java 
public class MyApplication extends Application<MyConfiguration> {

    @Override
    public void run(MyConfiguration configuration, Environment environment) {
        configuration.getAllowedPeers().registerAuthenticator(environment);
```

Finally, protect whatever resource endpoints you need with the Dropwizard @Auth annotation, like so:

```java
import io.dropwizard.auth.Auth;
import com.washingtonpost.dw.auth.model.Peer;

@Path("/api/stuff")
@Produces(MediaType.APPLICATION_JSON)
public class MyResource {

    @GET
    public Response getStuff(@Auth Peer peer, ...) {
        return stuff;
    }
```

The @Auth annotation will be hooked up to the AllowedPeerAuthenticator and will check every request to getStuff() to see if there's a BasicAuth header that authenticates against the passwords defined for users bob and alice in the provided allowed-peers.properties.

## Example configuration : plain strings

Some application deployment environments don't lend themselves to easily (d)encrypted properties files so an additional configuration option allows you to instead just provide a single string containing a list of usernames and a corresponding string containing passwords for those users, for example:
```yaml
allowedPeers: 
    users: alice;bob
    passwords: abc123;supersecret
```

Note that the default delimiter of ";" can be over-riden with the delimiter property like:

```yaml
allowedPeers: 
    users: alice/bob
    passwords: abc123/supersecret
    delimiter: /
```

If you use this configuration option, you must provide an equal number of usernames in the "users" string as you provide passwords in the "passwords" string, and you must provide both properties.

## Caching

As mentioned in https://dropwizard.github.io/dropwizard/manual/auth.html, caching may be an important concern if the backing stores for the authenticators is not capable of high throughput (this isn't really a concern for our flat file or strings, but caching support is provided for future extensibility).  If you provide a "cachePolicy" configuration option, the Authenticator that is registered with Jersey will be of the type CachingAuthenticator.  For example:

```yaml
allowedPeers: 
    credentialFile: allowed-peers.properties
    cachePolicy: maximumSize=100, expireAfterAccess=10m
```

## Realm name

BasicAuth challenges require a "realm" name which as far as I can tell isn't that important from a functional standpoint, so it defaults to "peers" but is configurable with the "realm" property like:
```yaml
allowedPeers: 
    credentialFile: allowed-peers.properties
    cachePolicy: maximumSize=100, expireAfterAccess=10m
    realm: SUPER SECRET STUFF
```


# TODO/Notes

* get this maven parented under the oss/sonatype parent so we can distribute it to maven central
* add checkstyle & better maven site generation
* support Chained Factories?