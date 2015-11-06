package com.washingtonpost.dw.auth.model;

import java.util.Objects;
import java.security.Principal;


/**
 * <p>Models a remote caller of this service</p>
 */
public class Peer implements Principal {
    private final String username;
    private final String password;

    public Peer(String username, String password) {
        this.username = username;
        this.password = password;
    }

    @Override
    public String toString() {
        return String.format("Peer{username=%s, password=%s}", this.username, this.password);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 59 * hash + Objects.hashCode(this.username);
        hash = 59 * hash + Objects.hashCode(this.password);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Peer other = (Peer) obj;
        if (!Objects.equals(this.username, other.username)) {
            return false;
        }
        if (!Objects.equals(this.password, other.password)) {
            return false;
        }
        return true;
    }

    @Override
    public String getName() {
        return this.username;
    }
}
