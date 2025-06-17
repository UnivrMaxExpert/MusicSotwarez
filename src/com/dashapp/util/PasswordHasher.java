package com.dashapp.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * PasswordHasher che fa l'hash della password e verifica se una password a quell'hash(utile per la sicurezza delle password)
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
