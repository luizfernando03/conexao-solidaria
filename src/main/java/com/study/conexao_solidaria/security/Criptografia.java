package com.study.conexao_solidaria.security;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Criptografia {

    public static String md5(String senha) throws NoSuchAlgorithmException {
        MessageDigest messagedig = MessageDigest.getInstance("MD5");
        BigInteger hash = new BigInteger(1, messagedig.digest(senha.getBytes()));
        return hash.toString(16);
    }

}
