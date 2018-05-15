package com.ariso.vertxproxy;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.shiro.codec.Hex;
import org.apache.sshd.common.util.Base64;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.buffer.impl.BufferFactoryImpl;
import io.vertx.core.buffer.impl.BufferImpl;
import io.vertx.core.spi.BufferFactory;

public class Encryptor {
	IvParameterSpec iv;
	SecretKeySpec skey;
	Cipher encipher;
	Cipher decipher;

	public Encryptor(String Key) throws Exception {
		KeyGenerator kgen = KeyGenerator.getInstance("AES");
		kgen.init(128, new SecureRandom(Key.getBytes()));
		SecretKey secretKey = kgen.generateKey();
		byte[] enCodeFormat = secretKey.getEncoded();

		iv = new IvParameterSpec(enCodeFormat);
		skey = new SecretKeySpec(enCodeFormat, "AES");
		encipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
		decipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
		encipher.init(Cipher.ENCRYPT_MODE, skey, iv);
		decipher.init(Cipher.DECRYPT_MODE, skey, iv);
	}

	public Buffer ConvertToEncrpyt(Buffer bf) throws Exception {

		Buffer result = Buffer.buffer();

		result.setInt(0, bf.length());
		result.setBytes(4, encipher.doFinal(bf.getBytes()));
		return result;

	}

	public Buffer ConvertToDecrpyt(Buffer bf) throws Exception {
		int buflen = bf.getInt(0);

		byte[] buf = decipher.doFinal(bf.getBytes(4, bf.length()));
		Buffer result = Buffer.buffer();
		result.setBytes(0, buf, 0, buflen);
		return result;

	}
 
	// Hex.encodeToString(bf.getBytes());
 

	public static void main(String[] args) throws Exception {
		String key = "Bar12345Bar12345";

		Encryptor aes = new Encryptor(key);
		Buffer bf = Buffer.buffer("Hello World");
		 
		System.out.println("Init           :" + Hex.encodeToString(bf.getBytes()));
		bf = aes.ConvertToEncrpyt(bf);
		System.out.println("ConvertToEncrpyt:" + Hex.encodeToString(bf.getBytes()));
		bf = aes.ConvertToDecrpyt(bf);
		System.out.println("ConvertToDecrpyt:" + Hex.encodeToString(bf.getBytes()));

	}
}
