package com.example.andr2project;

import android.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AppUtils {
//    private static final String SECRET_KEY = "aesEncryptionKey";
//    private static final String INIT_VECTOR = "encryptionIntVec";
//
//    //encrypt password
//    public static String encrypt(String value) {
//        try {
//            IvParameterSpec iv = new IvParameterSpec(INIT_VECTOR.getBytes("UTF-8"));
//            SecretKeySpec skeySpec = new SecretKeySpec(SECRET_KEY.getBytes("UTF-8"), "AES");
//
//            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
//            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);
//
//            byte[] encrypted = cipher.doFinal(value.getBytes());
//            return Base64.encodeToString(encrypted, Base64.DEFAULT);
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//        return null;
//    }
//
//    //decrypt password
//    public static String decrypt(String value) {
//        try {
//            IvParameterSpec iv = new IvParameterSpec(INIT_VECTOR.getBytes("UTF-8"));
//            SecretKeySpec skeySpec = new SecretKeySpec(SECRET_KEY.getBytes("UTF-8"), "AES");
//
//            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
//            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
//            byte[] original = cipher.doFinal(Base64.decode(value, Base64.DEFAULT));
//
//            return new String(original);
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//
//        return null;
//    }
}
