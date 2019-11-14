package com.ningpai.common.util;


import com.alibaba.fastjson.JSON;
import com.ningpai.site.util.GalaxyPayUtil;
import com.ningpai.util.MyLogger;

import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import java.io.ByteArrayOutputStream;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;


public class RSAUtils {
	private static final MyLogger LOGGER = new MyLogger(RSAUtils.class);
	
    /**
     * 加密算法RSA
     */
    private static final String KEY_ALGORITHM = "RSA";

    /**
     * 获取公钥的key
     */
    private static final String PUBLIC_KEY = "RSAPublicKey";

    /**
     * 获取私钥的key
     */
    private static final String PRIVATE_KEY = "RSAPrivateKey";

    /**
     * RSA最大加密明文大小
     */
    private static final int MAX_ENCRYPT_BLOCK = 117;

    /**
     * RSA最大解密密文大小
     */
    private static final int MAX_DECRYPT_BLOCK = 128;

    /**
     * <p>
     * 生成密钥对(公钥和私钥)
     * </p>
     *
     * @return 秘钥对
     */
    public static Map<String, Object> genKeyPair() throws Exception {
        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance(KEY_ALGORITHM);
        keyPairGen.initialize(1024, new SecureRandom());
        KeyPair keyPair = keyPairGen.generateKeyPair();
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();

        String publicKeyString = new String(Base64.encodeBase64(publicKey.getEncoded()));
        String privateKeyString = new String(Base64.encodeBase64(privateKey.getEncoded()));
        System.out.println("私钥：" + privateKeyString);
        System.out.println("公钥：" + publicKeyString);
        Map<String, Object> keyMap = new HashMap<String, Object>(2);
        keyMap.put(PUBLIC_KEY, publicKeyString);
        keyMap.put(PRIVATE_KEY, privateKeyString);
        return keyMap;
    }

    /**
     * <P>
     * 私钥解密
     * </p>
     *
     * @param encryptedData 已加密数据
     * @param privateKey    私钥(BASE64编码)
     * @return 明文
     */
    public static byte[] decryptByPrivateKey(byte[] encryptedData, String privateKey)
            throws Exception {
        byte[] keyBytes = Base64.decodeBase64(privateKey);
        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        Key privateK = keyFactory.generatePrivate(pkcs8KeySpec);
        Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
        cipher.init(Cipher.DECRYPT_MODE, privateK);
        int inputLen = encryptedData.length;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int offSet = 0;
        byte[] cache;
        int i = 0;
        // 对数据分段解密  
        while (inputLen - offSet > 0) {
            if (inputLen - offSet > MAX_DECRYPT_BLOCK) {
                cache = cipher.doFinal(encryptedData, offSet, MAX_DECRYPT_BLOCK);
            } else {
                cache = cipher.doFinal(encryptedData, offSet, inputLen - offSet);
            }
            out.write(cache, 0, cache.length);
            i++;
            offSet = i * MAX_DECRYPT_BLOCK;
        }
        byte[] decryptedData = out.toByteArray();
        out.close();
        return decryptedData;
    }

    /**
     * 私钥解密
     *
     * @param encryptedDataStr 密文
     * @param privateKey       私钥
     * @return 明文
     */
    public static String decryptByPrivateKey(String encryptedDataStr, String privateKey) {
        byte[] encryptedData = Base64.decodeBase64(encryptedDataStr);
        try {
            byte[] bytes = decryptByPrivateKey(encryptedData, privateKey);
            return new String(bytes);
        } catch (Exception e) {
            throw new RuntimeException("解密错误");
        }
    }

    /**
     * <p>
     * 公钥解密
     * </p>
     *
     * @param encryptedData 已加密数据
     * @param publicKey     公钥(BASE64编码)
     * @return 解密结果
     */
    public static byte[] decryptByPublicKey(byte[] encryptedData, String publicKey)
            throws Exception {
        byte[] keyBytes = Base64.decodeBase64(publicKey);
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        Key publicK = keyFactory.generatePublic(x509KeySpec);
        Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
        cipher.init(Cipher.DECRYPT_MODE, publicK);
        int inputLen = encryptedData.length;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int offSet = 0;
        byte[] cache;
        int i = 0;
        // 对数据分段解密  
        while (inputLen - offSet > 0) {
            if (inputLen - offSet > MAX_DECRYPT_BLOCK) {
                cache = cipher.doFinal(encryptedData, offSet, MAX_DECRYPT_BLOCK);
            } else {
                cache = cipher.doFinal(encryptedData, offSet, inputLen - offSet);
            }
            out.write(cache, 0, cache.length);
            i++;
            offSet = i * MAX_DECRYPT_BLOCK;
        }
        byte[] decryptedData = out.toByteArray();
        out.close();
        return decryptedData;
    }

    /**
     * 公钥解密
     *
     * @param encryptedDataStr 密文
     * @param publicKey        公钥
     * @return 明文
     */
    public static String decryptByPublicKey(String encryptedDataStr, String publicKey) {
        byte[] encryptedData = Base64.decodeBase64(encryptedDataStr);
        try {
            byte[] bytes = decryptByPublicKey(encryptedData, publicKey);
            return new String(bytes);
        } catch (Exception e) {
            throw new RuntimeException("解密失败");
        }
    }

    /**
     * <p>
     * 公钥加密
     * </p>
     *
     * @param data      源数据
     * @param publicKey 公钥(BASE64编码)
     * @return 加密结果
     */
    public static byte[] encryptByPublicKey(byte[] data, String publicKey)
            throws Exception {
    	LOGGER.info("开始加密的bate = encryptedData = "  + publicKey);
        byte[] keyBytes = null;
		try {
			keyBytes = Base64.decodeBase64(publicKey);
		} catch (Exception e) {
			LOGGER.info("keyBytes 获取失败 = "  + keyBytes);
			e.printStackTrace();
		}
        LOGGER.info("开始加密的bate = keyBytes = "  + keyBytes);
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        Key publicK = keyFactory.generatePublic(x509KeySpec);
        // 对数据加密  
        Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
        cipher.init(Cipher.ENCRYPT_MODE, publicK);
        int inputLen = data.length;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int offSet = 0;
        byte[] cache;
        int i = 0;
        // 对数据分段加密  
        while (inputLen - offSet > 0) {
            if (inputLen - offSet > MAX_ENCRYPT_BLOCK) {
                cache = cipher.doFinal(data, offSet, MAX_ENCRYPT_BLOCK);
            } else {
                cache = cipher.doFinal(data, offSet, inputLen - offSet);
            }
            out.write(cache, 0, cache.length);
            i++;
            offSet = i * MAX_ENCRYPT_BLOCK;
        }
        byte[] encryptedData = out.toByteArray();
        out.close();
        LOGGER.info("加密的bate = encryptedData = "  + encryptedData);
        return encryptedData;
    }

    /**
     * 公钥加密
     *
     * @param obj       待加密对象
     * @param publicKey 公钥
     * @return 密文
     */
    public static String encryptByPublicKey(Object obj, String publicKey) {
        try {
        	LOGGER.info("公钥加密 = publicKey = " + publicKey + ",obj = " + obj);
            byte[] bytes = encryptByPublicKey(JSON.toJSONBytes(obj), publicKey);
            LOGGER.info("公钥加密 = bytes = " + bytes);
            String aa = Base64.encodeBase64String(bytes);
            LOGGER.info("公钥加密 = aa = " + aa);
            return aa;
        } catch (Exception e) {
        	LOGGER.info("加密错误 e = " +  e.toString());
            throw new RuntimeException("加密错误");
        }
    }

    /**
     * <p>
     * 私钥加密
     * </p>
     *
     * @param data       源数据
     * @param privateKey 私钥(BASE64编码)
     * @return 密文
     */
    public static byte[] encryptByPrivateKey(byte[] data, String privateKey)
            throws Exception {
        byte[] keyBytes = Base64.decodeBase64(privateKey);
        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        Key privateK = keyFactory.generatePrivate(pkcs8KeySpec);
        Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
        cipher.init(Cipher.ENCRYPT_MODE, privateK);
        int inputLen = data.length;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int offSet = 0;
        byte[] cache;
        int i = 0;
        // 对数据分段加密  
        while (inputLen - offSet > 0) {
            if (inputLen - offSet > MAX_ENCRYPT_BLOCK) {
                cache = cipher.doFinal(data, offSet, MAX_ENCRYPT_BLOCK);
            } else {
                cache = cipher.doFinal(data, offSet, inputLen - offSet);
            }
            out.write(cache, 0, cache.length);
            i++;
            offSet = i * MAX_ENCRYPT_BLOCK;
        }
        byte[] encryptedData = out.toByteArray();
        out.close();
        return encryptedData;
    }

    /**
     * 私钥加密
     *
     * @param obj        待加密对象
     * @param privateKey 私钥
     * @return 密文
     */
    public static String encryptByPrivateKey(Object obj, String privateKey) {
        try {
            byte[] bytes = encryptByPrivateKey(JSON.toJSONBytes(obj), privateKey);
            return Base64.encodeBase64String(bytes);
        } catch (Exception e) {
            throw new RuntimeException("加密错误");
        }
    }

    public static void main(String[] args) throws Exception {
//        String privateKey = "MIICdQIBADANBgkqhkiG9w0BAQEFAASCAl8wggJbAgEAAoGBAIGJPd4LVcdLKBbpIxQH4MVze0nEcr8VRzJl6H9wBA4BCLbmz9AZcIVs82GlpegdhmQgNmJPY1ulfHu/FZaVIRBgyiw03xd4+viBrPQ8jUPZC7SbFQhqz29rUxVMcygbk0ry+gmU550VsCjcEIcKCyCddWKs8fkBQmDi7n5bNGK9AgMBAAECgYA5UsyoGIgLO70v9lfpo1raxjIHAQJugTISoXpz+tQwhlCe8CGCy985jG9gBPgYcghpAsBOXSdxBRjDglpBWxlYDkO+SNbZpkeMOvHhe4pM35NAWecqu0pLBxoweSUxJ3TnRsGVHwMoA6MKmhOMK99J1ppyKIUQVW8eAFXKmzIpAQJBAMePeQwPpbVa6AKMlf0tRdz/BrohEsa7ujwM1VjTxVQO8dHuduFbF75tEoCUMmswii/6vEiANkreB1qmmbwncd0CQQCmK+IhpoSpXP4YyRVOeJSXcEQM/HaQUCt0L0knvXKphbyBlkyFik4YOmZEVwsPxfhodn6cBqD9MG83W1/s6lZhAkB1xMHW0FUmlaNd/cF2TAVyyj4aEfKWvarGTXyKk09csAUdsvENRUjZ5FyK7OpkP50Ne5cp4iAoRqdoY/FODKkxAkArQ4uppTgu/EYv39LLTnupfhJJq0WXRzg5FMKgNSPwXSB6QM6Fij/Q8QuFSYBYx/tn/0nxh4xMv1qFeCXeghihAkBdDbpDEqppGAt10t34+TbN3k/gl7hjXz01Woj8+KJntp1VE0ralu8jAejB9fzexCcjlendKbvGnnjDz5vEU6Sj";
//        String publicKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCBiT3eC1XHSygW6SMUB+DFc3tJxHK/FUcyZeh/cAQOAQi25s/QGXCFbPNhpaXoHYZkIDZiT2NbpXx7vxWWlSEQYMosNN8XePr4gaz0PI1D2Qu0mxUIas9va1MVTHMoG5NK8voJlOedFbAo3BCHCgsgnXVirPH5AUJg4u5+WzRivQIDAQAB";
//        Object obj = 1;//new Object();
//        //私钥加密
//        String s = encryptByPrivateKey(obj, privateKey);
//        //公钥解密
//        String s1 = decryptByPublicKey("50FE6401E867A34BD533FE67BB85EDABFED62CEA9D8E3F5516E7B48D01F21A5F", publicKey);
//        //公钥加密
//        String s2 = encryptByPublicKey(obj, publicKey);
//        //私钥解密
//        String s3 = decryptByPrivateKey("50FE6401E867A34BD533FE67BB85EDABFED62CEA9D8E3F5516E7B48D01F21A5F", privateKey);
//        System.out.println(s1);
//        System.out.println(s3);
    	
    	genKeyPair();
    }
}
