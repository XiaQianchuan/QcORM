package com.xqc.qcorm.util;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * 安全加密工具
 * @author Xqc
 * @date 2013.7.23
 */
public class CryptoUtil {
	
	/**
	 * 信息摘要加密
	 * @param algorithm(MD5/SHA-256/SHA-512)
	 * @param plainText待加密字符串
	 * @return 已加密字符串
	 */
	public static String encrypt(String algorithm, String plainText) throws NoSuchAlgorithmException {
		MessageDigest md = MessageDigest.getInstance(algorithm);
		md.update(plainText.getBytes());
		byte[] b = md.digest();
		StringBuilder sb = new StringBuilder(32);
		for(int i = 0; i < b.length; i++) {
			String temp = Integer.toHexString(b[i]&0xff);
			if(temp.length() < 2) {
				sb.append("0");
			}
			sb.append(temp);
		}
		return sb.toString().toUpperCase();
	}
	
	/**
	 * 通用加密
	 * @param content
	 * @return key
	 */
	public static String generalEncrypt(String content) throws InvalidKeyException, NoSuchAlgorithmException, UnsupportedEncodingException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
		//Global key get from CryptoUtil.encrypt("MD5", "ECWARD2012XiaQianchuan")
		return aesEncrypt(content, "B0DB4EBF2AD77D4F2A5375DA8291E6B1");
	}
	
	/**
	 * 通用解密
	 * @param content
	 * @return
	 */
	public static String generalDecrypt(String content) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
		return aesDecrypt(content, "B0DB4EBF2AD77D4F2A5375DA8291E6B1");
	}

	/**
	 * AES-256加密
	 * @param content 待加密内容
	 * @param key 加密密钥
	 * @return 已加密内容
	 */
	public static String aesEncrypt(String content, String key) throws NoSuchAlgorithmException, UnsupportedEncodingException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		KeyGenerator kgen = KeyGenerator.getInstance("AES");
//		kgen.init(256,new SecureRandom(key.getBytes()));
		SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
        secureRandom.setSeed(key.getBytes());
		kgen.init(256,secureRandom);
		SecretKey secretKey = kgen.generateKey();
		byte[] enCodeFormat = secretKey.getEncoded();
		SecretKeySpec skey = new SecretKeySpec(enCodeFormat,"AES");
		Cipher cipher = Cipher.getInstance("AES");
		cipher.init(Cipher.ENCRYPT_MODE,skey);
		byte[] byteContent = content.getBytes();
		byte[] result = cipher.doFinal(byteContent);
		StringBuffer sb = new StringBuffer();
        for (int i=0;i<result.length;i++) {
            String hex = Integer.toHexString(result[i]&0xFF);
            sb.append(hex.length()==1?'0'+hex:hex);
        }
        return sb.toString();
	}
	
	/**
	 * AES-256解密
	 * @param content 待解密内容
	 * @param key 解密密钥
	 * @return 已解密内容
	 */
	public static String aesDecrypt(String content, String key) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		KeyGenerator kgen = KeyGenerator.getInstance("AES");
//		kgen.init(256,new SecureRandom(key.getBytes()));
		SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
        secureRandom.setSeed(key.getBytes());
		kgen.init(256,secureRandom);
		SecretKey secretKey = kgen.generateKey();
		byte[] enCodeFormat = secretKey.getEncoded();
		SecretKeySpec skey = new SecretKeySpec(enCodeFormat,"AES");
		Cipher cipher = Cipher.getInstance("AES");
		cipher.init(Cipher.DECRYPT_MODE,skey);
		byte[] byteContent = new byte[content.length()/2];
        for (int i=0;i<content.length()/2;i++) {
            int high = Integer.parseInt(content.substring(i*2,i*2+1),16);
            int low = Integer.parseInt(content.substring(i*2+1,i*2+2),16);
            byteContent[i] = (byte)(high*16+low);
        }
		return new String(cipher.doFinal(byteContent));
	}
}
