/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Utilities;

import java.util.Arrays;
import java.util.Random;
import java.util.regex.Pattern;

/**
 *
 * @author Exon
 */
public class PassGen {

    public String generate(char[] validchars, int len) {
        char[] password = new char[len];
        Random rand = new Random(System.nanoTime());
        for (int i = 0; i < len; i++) {
            password[i] = validchars[rand.nextInt(validchars.length)];
        }
        return new String(password);
    }

    public static final char[] getValid(final String regex, final int lastchar) {
        char[] potential = new char[lastchar]; // 32768 is not huge....
        int size = 0;
        final Pattern pattern = Pattern.compile(regex);
        for (int c = 0; c <= lastchar; c++) {
            if (pattern.matcher(String.valueOf((char) c)).matches()) {
                potential[size++] = (char) c;
            }
        }
        return Arrays.copyOf(potential, size);
    }

}
