package com.washingtonpost.dw.auth.dao;

import com.google.common.base.Preconditions;
import com.washingtonpost.dw.auth.encryptor.JasyptEncryptor;
import com.washingtonpost.dw.auth.model.Peer;
import java.util.HashSet;
import java.util.Set;

/**
 * <p>A simple implementation of a PeerDAO that assumes two strings contains a list of users and passwords in corresponding
 * places.  For example a user string of "bob,joe,jane" and a password string of "foo,bar,baz" would assume that bob's
 * password is "foo" and jane's password is "baz"</p>
 * <p>Gloming all users in a single String and all passwords in a single String is useful in the situation where your
 * application can only assume a single environment variable exported to it for users and a single environment variable
 * exported to it for passwords</p>
 */
public class StringPeerDAO implements PeerDAO {

    public static final String DEFAULT_DELIMITER = ";";
    private final Set<Peer> peers;


    /**
     * @param users A string containing one or more usernames
     * @param passwords A string containing as many passwords as there are users.  Note that while the source data may include
     * encrypted passwords wrapped in an "ENC(...)" string to indicate to humans that the password is encrypted, this
     * implementation strips off that ENC(...) wrapper to enable a simplify password-comparison logic.
     * @param delimiter The delimiter string for both {@code users} and {@code passwords}
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
            String username = userArray[i];
            Preconditions.checkState(nameIsUnique(peers, username), "Can't have 2 identical usernames");

            String password = JasyptEncryptor.getEncryptedPart(passArray[i]);

            peers.add(new Peer(username, password));
            //CHECKSTYLE_OFF: RegexpSinglelineJava
            System.out.println("Added peer " + username + " with password xxxx");
            //CHECKSTYLE_ON: RegexpSinglelineJava
        }
    }

    public StringPeerDAO(String users, String passwords) {
        this(users, passwords, DEFAULT_DELIMITER);
    }

    /**
     * @return Returns all the peers
     */
    @Override
    public Set<Peer> findAll() {
        return peers;
    }
}
