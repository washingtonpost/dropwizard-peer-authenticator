package com.washingtonpost.dw.auth.encryptor;

import java.util.regex.Matcher;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 * <p>
 * Tests some of the functionality of our JasyptEncryptor</p>
 */
public class TestJasyptEncryptor {

    @Test
    public void testRegexPattern() {
        Matcher matcher = JasyptEncryptor.PATTERN.matcher("ENC(blahblah)");
        if (matcher.find()) {
            assertEquals("blahblah", matcher.group(1));
        }
    }
}
