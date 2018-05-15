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

	SecretKeySpec skey;
	Cipher encipher;
	Cipher decipher;
	public Encryptor(String Key) throws Exception {
		KeyGenerator kgen = KeyGenerator.getInstance("AES");
		kgen.init(128, new SecureRandom(Key.getBytes()));
		SecretKey secretKey = kgen.generateKey();
		byte[] enCodeFormat = secretKey.getEncoded();
		skey = new SecretKeySpec(enCodeFormat, "AES");
		encipher = Cipher.getInstance("AES");
		decipher = Cipher.getInstance("AES");
		encipher.init(Cipher.ENCRYPT_MODE, skey);
		decipher.init(Cipher.DECRYPT_MODE, skey);
	}

	public void ConvertToEncrpyt(Buffer bf) throws Exception {
		
		bf.setBytes(0, encipher.doFinal(bf.getBytes()));
	}

	public void ConvertToDecrpyt(Buffer bf) throws Exception {
	 
		bf.setBytes(0, decipher.doFinal(bf.getBytes()));
	}
	
//	public void debugHex(Buffer bf)
//	{
//		 Hex.encodeToString(bf.getBytes());
//	}
 
	public static void main(String[] args) throws Exception {
		String key = "Bar12345Bar12345";  
	 
		Encryptor aes = new Encryptor(key);
		Buffer bf =    Buffer.buffer("Hello World");
		System.out.println("Init           :"+Hex.encodeToString(bf.getBytes()));
		aes.ConvertToEncrpyt(bf);
		System.out.println("ConvertToEncrpyt:"+Hex.encodeToString(bf.getBytes()));
		aes.ConvertToDecrpyt(bf);
		System.out.println("ConvertToDecrpyt:"+Hex.encodeToString(bf.getBytes()));
		 
	}
}
