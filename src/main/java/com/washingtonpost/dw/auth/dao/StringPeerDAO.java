package com.washingtonpost.dw.auth.dao;

import com.washingtonpost.dw.auth.model.Peer;
import java.util.Collection;
import java.util.HashSet;
import jersey.repackaged.com.google.common.base.Preconditions;

/**
 * <p>A simple implementation of a PeerDAO that assumes two strings contains a list of users and passwords in corresponding
 * places.  For example a user string of "bob,joe,jane" and a password string of "foo,bar,baz" would assume that bob's
 * password is "foo" and jane's password is "baz"</p>
 * <p>Gloming all users in a single String and all passwords in a single String is useful in the situation where your
 * application can only assume a single environment variable exported to it for users and a single environment variable
 * exported to it for passwords</p>
 */
public class StringPeerDAO implements PeerDAO {

    public final static String DEFAULT_DELIMITER = ";";
    private final Collection<Peer> peers;


    /**
     * @param users A string containing one or more usernames
     * @param passwords A string containing as many passwords as there are users
     * @param delimiter The delimiter string for both {@code users} and {@passwords}
     */
    public StringPeerDAO(String users, String passwords, String delimiter) {
        Preconditions.checkNotNull(users, "String containing users must not be null");
        Preconditions.checkNotNull(passwords, "String containing passwords must not be null");
        Preconditions.checkNotNull(delimiter, "Delimiter String must not be null");
        String[] userArray = users.split(delimiter);
        String[] passArray = passwords.split(delimiter);

        Preconditions.checkState(userArray.length == passArray.length, "Length of users (" + userArray.length + ") must match "
                + "the length of the passwords (" + passArray.length + ")");

        peers = new HashSet<>();
        for (int i=0; i<userArray.length; i++) {
            peers.add(new Peer(userArray[i], passArray[i]));
            System.out.println("Added peer " + userArray[i] + " with password " + passArray[i]);
        }
    }

    public StringPeerDAO(String users, String passwords) {
        this(users, passwords, DEFAULT_DELIMITER);
    }

    /**
     * @return Returns all the peers
     */
    @Override
    public Collection<Peer> findAll() {
        return peers;
    }
}
