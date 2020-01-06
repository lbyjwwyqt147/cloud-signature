package pers.liujunyi.cloud.signature.encrypt;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import java.io.ByteArrayOutputStream;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;


/**
 * RSA公钥/私钥/签名工具包
 *
 *
 * 字符串格式的密钥在未在特殊说明情况下都为BASE64编码格式<br/>
 * 由于非对称加密速度极其缓慢，一般文件不使用它来加密而是使用对称加密，<br/>
 * 非对称加密算法可以用来对对称加密的密钥加密，这样保证密钥的安全也就保证了数据的安全
 *
 * @author ljy
 * @date 2019-03-25
 * @version 1.0
 */
@Log4j2
public class RsaEncryptUtils {

    /**
     * 加密算法RSA
     */
    public static final String KEY_ALGORITHM = "RSA";

    /**
     * 签名算法
     */
    public static final String SIGNATURE_ALGORITHM = "MD5withRSA";

    /**
     * RSA最大加密明文大小
     */
    private static final int MAX_ENCRYPT_BLOCK = 117;

    /**
     * RSA最大解密密文大小
     */
    private static final int MAX_DECRYPT_BLOCK = 128;

    private static KeyFactory keyFactory = null;

    static {
        try {
            keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }



    /**
     * 生成密钥对(公钥和私钥)
     *
     * @return
     * @throws Exception
     */
    public static void genKeyPair() throws Exception {
        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance(KEY_ALGORITHM);
        keyPairGen.initialize(1024);
        KeyPair keyPair = keyPairGen.generateKeyPair();
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
        log.info("RSA 公钥:" + Base64.encodeBase64String(publicKey.getEncoded()));
        log.info("RSA 私钥:" + Base64.encodeBase64String(privateKey.getEncoded()));
    }

    /**
     * 用私钥对信息生成数字签名
     *
     * @param data 待签名的数据
     * @param privateKey 私钥(BASE64编码)
     *
     * @return 签名
     * @throws Exception
     */
    public static String sign(String data, String privateKey) throws Exception {
        PrivateKey  key = getPrivateKeyFromBase64KeyEncodeStr(privateKey);
        Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
        signature.initSign(key);
        signature.update(data.getBytes());
        return new String(Base64.encodeBase64(signature.sign()));
    }

    /**
     * 校验数字签名
     *
     * @param data 原始数据
     * @param publicKey 公钥(BASE64编码)
     * @param sign 数字签名
     *
     * @return 是否签名通过
     * @throws Exception
     *
     */
    public static boolean verify(String data, String publicKey, String sign)
            throws Exception {
        PublicKey  key = getPublicKeyFromBase64KeyEncodeStr(publicKey);
        Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
        signature.initVerify(key);
        signature.update(data.getBytes());
        return signature.verify(Base64.decodeBase64(sign.getBytes()));
    }


    /**
     * 获取base64加密后的字符串的原始私钥
     * @param key  私钥base64字符串
     * @return
     */
    public  static PrivateKey getPrivateKeyFromBase64KeyEncodeStr(String key) {
        byte[] keyBytes = Base64.decodeBase64(key);
        // 取得私钥
        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyBytes);
        PrivateKey  privateKey = null;
        try {
            privateKey = keyFactory.generatePrivate(pkcs8KeySpec);
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return privateKey;
    }

    /**
     * 获取base64加密后的字符串的原始公钥
     * @param key  公钥base64字符串
     * @return
     */
    public static PublicKey getPublicKeyFromBase64KeyEncodeStr(String key) {
        byte[] keyBytes = Base64.decodeBase64(key);
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(keyBytes);
        PublicKey  publicKey = null;
        try {
            publicKey = keyFactory.generatePublic(x509KeySpec);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return publicKey;
    }

    /**
     * 私钥 解密方法
     * @param data 要解密的数据
     * @param privateKey
     * @return 解密后的原数据
     * @throws Exception
     */
    public static String decrypt(String data, String privateKey) throws Exception{
        //要加密的数据
        log.info("要解密的数据:" + data);
        //对私钥解密
        PrivateKey decodePrivateKey = getPrivateKeyFromBase64KeyEncodeStr(privateKey);
        Cipher cipher = Cipher.getInstance(KEY_ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, decodePrivateKey);
        byte[] encodedData = Base64.decodeBase64(data);
        byte[] decodedData = cipher.doFinal(encodedData);
        String decodedDataStr = new String(decodedData,"utf-8");
        log.info("私钥解密后的数据:"+decodedDataStr);
        return decodedDataStr;
    }

    /**
     * 公钥加密方法
     * @param primitiveData 要加密的数据
     * @param publicKey 公钥base64字符串
     * @return 加密后的base64字符串
     * @throws Exception
     */
    public static String encrypt(String primitiveData, String publicKey) throws Exception{
        //要加密的数据
        log.info("要加密的数据:" + primitiveData);
        byte[] data = primitiveData.getBytes();
        // 对公钥解密
        Key decodePublicKey = getPublicKeyFromBase64KeyEncodeStr(publicKey);
        // 对数据加密
        Cipher cipher = Cipher.getInstance(KEY_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, decodePublicKey);
        byte[] encodedData = cipher.doFinal(data);
        String encodedDataStr = new String(Base64.encodeBase64(encodedData));
        log.info("公钥加密后的数据:" + encodedDataStr);
        return encodedDataStr;
    }


    /**
     * 使用公钥进行分段加密
     * @param primitiveData 要加密的数据
     * @return 公钥base64字符串
     * @throws Exception
     */
    public static String encryptByPublicKey(String primitiveData, String publicKey) throws Exception {
        //要加密的数据
        log.info("要加密的数据:" + primitiveData);
        byte[] data = primitiveData.getBytes();
        // 对公钥解密
        PublicKey decodePublicKey = getPublicKeyFromBase64KeyEncodeStr(publicKey);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        // 对数据加密
        Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
        cipher.init(Cipher.ENCRYPT_MODE, decodePublicKey);
        int inputLen = data.length;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        //偏移量
        int offSet = 0;
        int i = 0;
        // 对数据分段加密
        while (inputLen - offSet > 0) {
            byte[] cache;
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
        String encodedDataStr = Base64.encodeBase64String(encryptedData);
        log.info("公钥加密后的数据:" + encodedDataStr);
        return encodedDataStr;
    }


    /**
     * 使用私钥进行分段解密
     * @param data 使用base64处理过的密文
     * @param privateKey  私钥
     * @return 解密后的数据
     * @throws Exception
     */
    public static String decryptByPrivateKey(String data, String privateKey)
            throws Exception {
        byte[] encryptedData = Base64.decodeBase64(data);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        PrivateKey decodePrivateKey = getPrivateKeyFromBase64KeyEncodeStr(privateKey);
        Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
        cipher.init(Cipher.DECRYPT_MODE, decodePrivateKey);
        int inputLen = encryptedData.length;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int offSet = 0;
        int i = 0;
        // 对数据分段解密
        while (inputLen - offSet > 0) {
            byte[] cache;
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
        String decodedDataStr = new String(decryptedData,"utf-8");
        log.info("私钥解密后的数据:" + decodedDataStr);
        return decodedDataStr;
    }


    public static void main(String[] args) throws Exception {

        //获取公钥/私钥
       // genKeyPair();
        String publicKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCEcSZYMyMDO0qSPaim/k34wKBENmDo8TYZdlQUPNjWrNm/w4lxTAOqSbgAbAzmlsTzNuHXE/5BHQwxnSvGwKWTY/zlj7N04eEgl60Fy1Jq0v/pRZmYpnZfVztAjqEeCgclNmbRESgTuTyNBCVn3Jn9vEu3oID95i5dsEDOb8YKvwIDAQAB";
        String privateKey = "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBAIRxJlgzIwM7SpI9qKb+TfjAoEQ2YOjxNhl2VBQ82Nas2b/DiXFMA6pJuABsDOaWxPM24dcT/kEdDDGdK8bApZNj/OWPs3Th4SCXrQXLUmrS/+lFmZimdl9XO0COoR4KByU2ZtERKBO5PI0EJWfcmf28S7eggP3mLl2wQM5vxgq/AgMBAAECgYAtQOBCOM7Y1tSHHYxX6wLHYUIhrJb2YR0EHDtkYtoQmCUa8fwvezKxZ9j4Q/MwgwmutOz76Xfx1bQJ69M+aSrpr9JF7SVDRl0TFOInCVICkwfKufAex21D40wLgRV9+hyiwLTUuVor3yLEgFGohModc4e8XxF5wpm6taEyoCAgeQJBANHdMV7s99ieJHCSM0yBRjXv8/eJz/k/5FysFFHzM7RH3Qi1guDDNfxXLCv057GMtOPFs1/P6gK5wcL8LmlCLrsCQQChjsUg0FxpwaL4mJxtZ8BUjjTHfuJ78JSmH0nm524Gvnu17yjrDD3bF6k4bGVAdQFx1n0UzjKDq+FFJeSAE23NAkEAsZ7wplMSAjj2xoA3As13Szdn2V6+s0qsUPMjz6hzXmZkYXae6vTNwGFXdWy2nMNmZlFx2+nxOZVWtV0TOutU+QJAJBtbdUz8CmrLeJHrDAyPEJbDtv5lsdt/7Wy6wI9iqMEztuKfm4Cd5nRwTnrzWieMThvo0piO85ybeS/R1MoC1QJAeUihTgTrQ4YFNr4KEgfvm0f3d5tHQMXXGd8lpEDDjRI/3b/v6goG5b48dfzAPppLDvbxqYGcYO3tOpwXWLlMtw==";


        //普通字符 加密原始数据
        String data1 = "11-11-11 普通字符 加密原始数据";
        String encrypt1 = encrypt(data1, publicKey);
        System.out.println("普通加密后:" + encrypt1);
        System.out.println("普通解密后:" + decrypt(encrypt1, privateKey));

        //超长字符 加密原始数据
        String data = "{\"data\":[{\"children\":[{\"functionButtonGroup\":[\"1\",\"2\",\"3\",\"4\",\"5\",\"6\",\"7\",\"8\",\"9\",\"10\"],\"id\":10,\"menuIcon\":\"la-outdent\",\"menuOpenUrl\":\"../dict/dict.html\",\"moduleCode\":\"1010\",\"moduleName\":\"数据字典\",\"modulePid\":1,\"moduleType\":2,\"status\":0},{\"functionButtonGroup\":[\"1\",\"2\",\"3\",\"4\",\"5\",\"6\",\"7\",\"8\",\"9\",\"10\"],\"id\":11,\"menuIcon\":\"la-outdent\",\"menuOpenUrl\":\"../organization/organization.html\",\"moduleCode\":\"1011\",\"moduleName\":\"组织机构\",\"modulePid\":1,\"moduleType\":2,\"status\":0}],\"id\":1,\"menuIcon\":\"flaticon-cogwheel\",\"menuOpenUrl\":\"\",\"moduleCode\":\"1001\",\"moduleName\":\"系统设置\",\"modulePid\":0,\"moduleType\":1,\"status\":0},{\"children\":[{\"functionButtonGroup\":[\"1\",\"2\",\"3\",\"4\",\"5\",\"6\",\"7\",\"8\",\"9\",\"10\"],\"id\":20,\"menuIcon\":\"la-outdent\",\"menuOpenUrl\":\"../photo/album/fahrenheit/fahrenheit.html\",\"moduleCode\":\"1020\",\"moduleName\":\"写真集\",\"modulePid\":2,\"moduleType\":2,\"status\":0},{\"functionButtonGroup\":[\"1\",\"2\",\"3\",\"4\",\"5\",\"6\",\"7\",\"8\",\"9\",\"10\"],\"id\":21,\"menuIcon\":\"la-outdent\",\"menuOpenUrl\":\"../photo/album/wang/wang.html\",\"moduleCode\":\"1021\",\"moduleName\":\"婚纱照\",\"modulePid\":2,\"moduleType\":2,\"status\":0},{\"functionButtonGroup\":[\"1\",\"2\",\"3\",\"4\",\"5\",\"6\",\"7\",\"8\",\"9\",\"10\"],\"id\":22,\"menuIcon\":\"la-outdent\",\"menuOpenUrl\":\"../photo/album/carousel/carousel.html\",\"moduleCode\":\"1022\",\"moduleName\":\"轮播图\",\"modulePid\":2,\"moduleType\":2,\"status\":0}],\"id\":2,\"menuIcon\":\"flaticon-web\",\"menuOpenUrl\":\"\",\"moduleCode\":\"1002\",\"moduleName\":\"相册管理\",\"modulePid\":0,\"moduleType\":1,\"status\":0},{\"children\":[{\"functionButtonGroup\":[\"1\",\"2\",\"3\",\"4\",\"5\",\"6\",\"7\",\"8\",\"9\",\"10\"],\"id\":30,\"menuIcon\":\"la-outdent\",\"menuOpenUrl\":\"../photo/users/staff/staff.html\",\"moduleCode\":\"1030\",\"moduleName\":\"员工管理\",\"modulePid\":3,\"moduleType\":2,\"status\":0},{\"functionButtonGroup\":[\"1\",\"2\",\"3\",\"4\",\"5\",\"6\",\"7\",\"8\",\"9\",\"10\"],\"id\":31,\"menuIcon\":\"la-outdent\",\"menuOpenUrl\":\"../photo/users/customer/customer.html\",\"moduleCode\":\"1031\",\"moduleName\":\"顾客管理\",\"modulePid\":3,\"moduleType\":2,\"status\":0}],\"id\":3,\"menuIcon\":\"flaticon-users\",\"menuOpenUrl\":\"\",\"moduleCode\":\"1003\",\"moduleName\":\"用户管理\",\"modulePid\":0,\"moduleType\":1,\"status\":0}],\"message\":\"success.\",\"status\":200,\"success\":true,\"timestamp\":\"2019-03-25 15:44:53\"}";
        String encrypt = encryptByPublicKey(data, publicKey);
        System.out.println("分段加密后:" + encrypt);
        String test = "ovoxDd+ns/sewWRzaHKPYzFT8l07MC0f1FZqnaqxLWHCm0hrpdesQq8uj1mQvjp7U5fwxmEJPBxlB83vlOmQJ3W7ImwSF9p5PzWcrbE5H6KyLgK6T944M6+DcgRCisRbV5Jf/2emBRRzWKda4d8OkPl2Usz8Joc00s4JZc5e1tvRbH/y0+2I1GoSf3b4cGwSZXRJEEp6UjhXDlRYk6tfDdh+yhu+2fdZ2WC1WHgjVtYX5TBV5p5DWNOEG3GmAgGRpNgVWpZO6FjxLVBJNWLTxQx3iVgunObY83eKMq+cyYoUYPjgJVpIEribEZVgp/ltmztc0k6QB6ed4Drbqh1jVgdcPeYWPzT4I0cRD0RwDSgT8lEhL3Ux5R4fCQNHHoVBxPwtoT7Hxj+ZuWMyztvIg6xNqURFJ06W4HFYo22MudI=";
        System.out.println("分段解密后:" + decryptByPrivateKey(test, privateKey));
    }

}
