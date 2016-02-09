package com.washingtonpost.dw.auth.encryptor;

import com.beust.jcommander.JCommander;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>Main() class for encrypting passwords</p>
 * <p>See our README.md for documentation</p>
 */
public final class JasyptEncryptor {

    public static final Pattern PATTERN = Pattern.compile("ENC\\((.+)\\)");

    private JasyptEncryptor() {
    }

    /**
     * @param encString An ecnrypted string wrapped in the literal "ENC("...")" strings like "ENC(21u39fjvi0j0)"
     * @return The "encrypted part", e.g. "21u39fjvi0j0", or the original (untouched) encString if it doesn't match that pattern
     */
    public static String getEncryptedPart(String encString) {
        Matcher matcher = JasyptEncryptor.PATTERN.matcher(encString);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return encString;
    }

    /**
     * @param args Assumes a non-null "-type" param and "-password" param for use in creating an encryptor and then
     * encrypting the secret.
     */
    public static void main(String[] args) {
        JasyptEncryptorParams params = new JasyptEncryptorParams();
        JCommander jCommander = new JCommander(params, args);

        String encryptedPassword = String.format("ENC(%s)", params.getEncryptor().encryptPassword(params.password));
        // CHECKSTYLE_OFF: RegexpSinglelineJava
        System.out.println(encryptedPassword);
    }
}