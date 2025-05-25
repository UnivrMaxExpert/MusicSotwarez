package com.dashapp.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * PasswordHasher grants the possibility to migrate to other libraries
 */
public class PasswordHasher
{
    public static String hashPassword(String plainPassword) {
        // Genera un salt casuale (il salt viene utilizzato per rendere unico l'hash)
        String salt = BCrypt.gensalt();
        // Crea l'hash della password concatenandolo al salt
        return BCrypt.hashpw(plainPassword, salt);
    }
    public static boolean checkPassword(String plainPassword, String hashedPassword) {
        return BCrypt.checkpw(plainPassword, hashedPassword);
    }
}
