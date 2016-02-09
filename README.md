# dropwizard-peer-authenticator
Dropwizard module to enable BasicAuth security around a service with convenience factories for reading in lists of (users, passwords) who are authorized to make requests of your service.

This artifact provides a Dropwizard Configuration/Factory class that enables convenient registration of an authorization filter with a Dropwizard Jersey Server.  This artifact is essentially a configuration wrapper around the documentation provided at http://www.dropwizard.io/0.9.2/docs/manual/auth.html

A "Peer" object is a (username, password) POJO that models a remote service or user invoking some endpoint in your Dropwizard service.  The AllowedPeerAuthenticator loads a list of allowed peers from some source and then registers itself with Jersey to provide endpoint-level authentication on top of HTTP BasicAuth.  Because this is just a BasicAuth authenticator, your DW service should only be accessed over HTTPS.

## Maven dependency
Check [./RELEASE_NOTES.md](./RELEASE_NOTES.md) for the latest/best version for your needs, and add this to your pom:
```
<dependency>
    <groupId>com.washingtonpost.dropwizard</groupId>
    <artifactId>dropwizard-peer-authenticator</artifactId>
    <version>2.1.0-SNAPSHOT</version>
</dependency>
```

In general, the 1.x.y versions are compatible with Dropwizard-0.8-X while the 2.x.y versions are compatible with Dropwizard-0.9.X

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

As mentioned in http://www.dropwizard.io/0.9.2/docs/manual/auth.html, caching may be an important concern if the backing stores for the authenticators is not capable of high throughput (this isn't really a concern for our flat file or strings, but caching support is provided for future extensibility).  If you provide a "cachePolicy" configuration option, the Authenticator that is registered with Jersey will be of the type CachingAuthenticator.  For example:

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


## Encrypting Passwords

To avoid plain-text passwords in your allowed-peers.properties file, this module enables you to specify a simple {"NONE", "BASIC" or "STRONG"} encryption policy on the supplied passwords which correspond to none, Basic or Strong PasswordEncryptors from the excellent http://www.jasypt.org/ project.  For value "NONE" it's assumed the allowed-peers.properties contains plaintext passwords and requests to the running service have unencrypted passwords in their BasicAuth header.  If a "BASIC" or "STRONG" encryptor configuration is provided, then it's assumed the passwords in allowed-peers.properties are encrypted with the Jasypt PasswordEncryptor and that the BasicAuth passwords are _unencrypted_ but will be encrypted using the same encryptor before being compared against the encrypted allowed-peers.properties value.

For example, to enable encrypting of the passwords in your allowed-peers.properties file with Jasypt's BasicPasswordEncryptor, add this configuration to your Dropwizard YAML configuration file:

```yaml
allowedPeers:
    credentialFile: allowed-peers.properties
    encryptor: BASIC
```

Then encrypt the password in your `allowed-peers.properties` files using a `main` class shipped in this JAR:
```
git clone git@github.com:washingtonpost/dropwizard-peer-authenticator.git
mvn clean install -Pexecutable-jar

java -cp target/dropwizard-peer-authenticator-*-SNAPSHOT.jar com.washingtonpost.dw.auth.encryptor.JasyptEncryptor -type BASIC -password
The secret to encrypt: <mySecret>
ENC(LrAsd3MBh/grqOMIMdtO1UQ0Mavz+U1s)

## or, for stronger/slightly slower security use the "STRONG" type
## just make sure your YAML file declares the same allowedPeers.encryptor type as you used when encrypting your passwords
java -cp target/dropwizard-peer-authenticator-*-SNAPSHOT.jar com.washingtonpost.dw.auth.encryptor.JasyptEncryptor -type STRONG -password
The secret to encrypt: <mySecret>
ENC(1XuMDHrI3yxbX5dMngRMn6n2RUD3XiAjr1hRdlkLzsBUWaVifl9GBd6q/cokEUt6)
```

Use the encrypted password in your allowed-peers.properties file, e.g.:

```yaml
alice=ENC(LrAsd3MBh/grqOMIMdtO1UQ0Mavz+U1s)
```

Then any BasicAuth requests made against a service protected with this allowed-peers module should pass "alice:mySecret" as the username and password which will authenticate against the encrypted, in-memory "alice:LrAsd3MBh/grqOMIMdtO1UQ0Mavz+U1s" value.


# TODO/Notes

* add checkstyle & better maven site generation
* support Chained Factories?
