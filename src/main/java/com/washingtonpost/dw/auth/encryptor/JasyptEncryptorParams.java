package com.washingtonpost.dw.auth.encryptor;

import com.beust.jcommander.Parameter;
import com.washingtonpost.dw.auth.AllowedPeerConfiguration.Encryptor;
import org.jasypt.util.password.PasswordEncryptor;

/**
 * <p>JCommander possible-command-line-parameters struct</p>
 * <p>For more info, see http://jcommander.org/</p>
 */
public class JasyptEncryptorParams {
    @Parameter(required=true, names="-type", description="The type of encryptor to use {BASIC or STRONG}")
    String type;

    @Parameter(required=true, names={"-password"}, description="The secret to encrypt", password=true)
    String password;

    PasswordEncryptor getEncryptor() {
        return Encryptor.valueOf(type).getPasswordEncryptor();
    }
}