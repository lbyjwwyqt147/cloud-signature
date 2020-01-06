package pers.liujunyi.cloud.signature.encrypt;

import com.alibaba.fastjson.JSON;
import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/***
 *
 * @author ljy
 */
public class AesEncryptUtils {
	private static final String KEY = "dO6+g3+08ELBKtx/1/WBYQ==";
	private static final String ALGORITHMSTR = "AES/ECB/PKCS5Padding";
	private static final Integer KEY_LENGTH = 128;

	/**
	 * 生成密钥
	 * @return
	 * @throws Exception
	 */
	public static String generateDesKey() throws Exception {
		//实例化
		KeyGenerator kgen = null;
		kgen = KeyGenerator.getInstance("AES");
		//设置密钥长度
		kgen.init(KEY_LENGTH);
		//生成密钥
		SecretKey skey = kgen.generateKey();
		//返回密钥
		return Base64.encodeBase64String(skey.getEncoded());
	}


	/**
	 * base64 加密
	 * @param bytes
	 * @return
	 */
	public static String base64Encode(byte[] bytes) {
		return Base64.encodeBase64String(bytes);
	}

	/**
	 * base64 解密
	 * @param base64Code
	 * @return
	 */
	public static byte[] base64Decode(String base64Code) throws Exception {
		return Base64.decodeBase64(base64Code);
	}

	public static byte[] aesEncryptToBytes(String content, String encryptKey) throws Exception {
		KeyGenerator kgen = KeyGenerator.getInstance("AES");
		kgen.init(KEY_LENGTH);
		Cipher cipher = Cipher.getInstance(ALGORITHMSTR);
		cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(encryptKey.getBytes(), "AES"));
		return cipher.doFinal(content.getBytes("utf-8"));
	}

	/**
	 * 对数据进行加密
	 * @param content  要加密的数据
	 * @param encryptKey 密钥
	 * @return
	 * @throws Exception
	 */
	public static String aesEncrypt(String content, String encryptKey) {
		try {
			return base64Encode(aesEncryptToBytes(content, encryptKey));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 对数据进行加密
	 * @param content  要加密的数据
	 * @param encryptKey 密钥
	 * @return
	 * @throws Exception
	 */
	public static String aesEncrypt(Object content, String encryptKey) {
		try {
			return base64Encode(aesEncryptToBytes(JSON.toJSONString(content), encryptKey));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 解密数据
	 * @param encryptBytes
	 * @param decryptKey
	 * @return
	 * @throws Exception
	 */
	public static String aesDecryptByBytes(byte[] encryptBytes, String decryptKey) throws Exception {
		KeyGenerator kgen = KeyGenerator.getInstance("AES");
		kgen.init(KEY_LENGTH);
		Cipher cipher = Cipher.getInstance(ALGORITHMSTR);
		cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(decryptKey.getBytes(), "AES"));
		byte[] decryptBytes = cipher.doFinal(encryptBytes);
		return new String(decryptBytes);
	}

	/**
	 * 对加密数据 进行解密
	 * @param encryptStr
	 * @param decryptKey
	 * @return
	 * @throws Exception
	 */
	public static String aesDecrypt(String encryptStr, String decryptKey) {
		try {
			return aesDecryptByBytes(base64Decode(encryptStr), decryptKey);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 对加密数据 进行解密
	 * @param encryptStr
	 * @param decryptKey
	 * @return
	 * @throws Exception
	 */
	public static String aesDecrypt(Object encryptStr, String decryptKey) {
		try {
			return aesDecryptByBytes(base64Decode(JSON.toJSONString(encryptStr)), decryptKey);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static void main(String[] args) throws Exception {
		//System.out.println(generateDesKey());
		String content = "            out.write(cache, 0, cache.length);";
		System.out.println("加密前：" + content);

		String encrypt = aesEncrypt(content, KEY);
		System.out.println(encrypt.length() + ":加密后：" + encrypt);


        String test = "EOqRKqCCF/xRJWrqRaxN0Z6HyislNf+F1mTapZcjhgYg/B9uUrIvJhx8LusM4uzQO0x1k6rzNxPrj/phNnmoSCfiRvaB6bUtO7OMoxn5fbFe/GQ0mMV2oxig+Vs6jJRzMUIY87onlqtFiPTvaIY74iaDpIbY4zXIsxBkVv/F2y1Iq3gl8idQXwUzC4VwbS42ILdWnLWaiYcokHUNIayk1HXWhOLk15JtKuvnw05cxbO4gSLqt04MYmYli3Y5+5CUXYXIH3tgOk7EpOpTianEXdBkFdosQkRJHhXiW7TaSKhobFw+pzhVOxKH7aP9hV3NkVAuYsbQtPgwlsUPBFSL7qVTTk/cnyDJhsn4eSXkUOk5PU3yXVlZ9loTQD8kdjYap0sYozxIGrtI6qmU5Ah0fw==";

		String decrypt = aesDecrypt(test, KEY);
		System.out.println("解密后：" + decrypt);
	}
	
}