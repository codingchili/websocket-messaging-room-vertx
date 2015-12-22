package com.websocket.frontend.room;

import com.websocket.frontend.room.Exception.TokenException;
import com.websocket.frontend.room.Protocol.Token;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;

/**
 * Created by Robin on 2015-12-22.
 * <p>
 * Verifies that a token has been signed by a backend server.
 */
class Authentication {
    private static final byte[] SECRET = "0000000000000000000000000000000000000000000000000000000000000000".getBytes();
    private static final String ALGORITHM = "HmacSHA512";

    /**
     * Checks if a token and its parameters is valid against the secret.
     *
     * @param token    hex encoded token to be verified.
     * @param username the username of the requestor.
     * @param expiry   the unix epoch time in which it is expired.
     * @return true if the token is accepted.
     */
    public static boolean VerifyToken(String token, String username, Long expiry) {
        if (expiry > Instant.now().getEpochSecond()) {
            try {
                byte[] result = DatatypeConverter.printHexBinary(GenerateToken(username, expiry)).getBytes();

                return ConstantTimeCompare(result, token.toUpperCase().getBytes());
            } catch (NoSuchAlgorithmException | InvalidKeyException ignored) {
            }
        }
        return false;
    }

    /**
     * @param token Containing token data.
     * @return true if the token is accepted.
     * @see #VerifyToken(String token, String username, Long expiry)
     */
    public static boolean VerifyToken(Token token) {
        return (VerifyToken(token.getKey(), token.getUsername(), token.getExpiry()));
    }

    private static byte[] GenerateToken(String username, Long expiry) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac mac = Mac.getInstance(ALGORITHM);

        SecretKeySpec key = new SecretKeySpec(SECRET, ALGORITHM);
        mac.init(key);

        mac.update(username.getBytes());
        mac.update(expiry.toString().getBytes());

        return mac.doFinal();
    }

    /**
     * Generates a new token from a given username.. be careful..
     *
     * @param username the token should be signed with.
     * @param expiry   indicates when the token expires.
     * @return a signed token as a base64 string.
     */
    protected static String SignToken(String username, long expiry) throws TokenException {
        try {
            return DatatypeConverter.printHexBinary(GenerateToken(username, expiry));
        } catch (InvalidKeyException | NoSuchAlgorithmException e) {
            throw new TokenException();
        }
    }

    private static boolean ConstantTimeCompare(byte[] first, byte[] second) {
        int result = 0;

        if (first.length != second.length)
            return false;

        for (int i = 0; i < first.length; i++)
            result |= (first[i] ^ second[i]);

        return result == 0;
    }
}
