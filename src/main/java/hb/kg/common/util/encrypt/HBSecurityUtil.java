package hb.kg.common.util.encrypt;

import java.util.Map;

import hb.kg.common.util.encrypt.coder.BASE64Encoder;
import hb.kg.common.util.encrypt.coder.DESCoder;
import hb.kg.common.util.encrypt.coder.HmacCoder;
import hb.kg.common.util.encrypt.coder.MDCoder;
import hb.kg.common.util.encrypt.coder.RSACoder;
import hb.kg.common.util.encrypt.coder.SHACoder;

/**
 * 数据加密辅助类(默认编码UTF-8)， 该方法是静态的，在Service中还要提供一个Security的服务，服务中配置好加解密的各类参数
 */
public final class HBSecurityUtil {
    private HBSecurityUtil() {}

    /**
     * 默认算法密钥
     */
    private static final byte[] ENCRYPT_KEY = { -81, 0, 105, 7, -32, 26, -49, 88 };
    public static final String CHARSET = "UTF-8";

    /**
     * BASE64解码
     * @param str
     * @return
     */
    public static final byte[] decryptBASE64(String str) {
        try {
            return BASE64Encoder.decode(str);
        } catch (Exception e) {
            throw new RuntimeException("解密错误，错误信息：", e);
        }
    }

    /**
     * BASE64编码
     * @param str
     * @return
     */
    public static final String encryptBASE64(byte[] str) {
        try {
            return BASE64Encoder.encode(str);
        } catch (Exception e) {
            throw new RuntimeException("加密错误，错误信息：", e);
        }
    }

    /**
     * 数据解密，算法（DES）
     * @param cryptData 加密数据
     * @return 解密后的数据
     */
    public static final String decryptDes(String cryptData) {
        return decryptDes(cryptData, ENCRYPT_KEY);
    }

    /**
     * 数据加密，算法（DES）
     * @param data 要进行加密的数据
     * @return 加密后的数据
     */
    public static final String encryptDes(String data) {
        return encryptDes(data, ENCRYPT_KEY);
    }

    /**
     * 基于MD5算法的单向加密
     * @param strSrc 明文
     * @return 返回密文
     */
    public static final String encryptMd5(String strSrc) {
        String outString = null;
        try {
            outString = encryptBASE64(MDCoder.encodeMD5(strSrc.getBytes(CHARSET)));
        } catch (Exception e) {
            throw new RuntimeException("加密错误，错误信息：", e);
        }
        return outString;
    }

    /**
     * SHA加密
     * @param data
     * @return
     */
    public static final String encryptSHA(String data) {
        try {
            return encryptBASE64(SHACoder.encodeSHA256(data.getBytes(CHARSET)));
        } catch (Exception e) {
            throw new RuntimeException("加密错误，错误信息：", e);
        }
    }

    /**
     * 数据解密，算法（DES）
     * @param cryptData 加密数据
     * @param key
     * @return 解密后的数据
     */
    public static final String decryptDes(String cryptData,
                                          byte[] key) {
        String decryptedData = null;
        try {
            // 把字符串解码为字节数组，并解密
            decryptedData = new String(DESCoder.decrypt(decryptBASE64(cryptData), key));
        } catch (Exception e) {
            throw new RuntimeException("解密错误，错误信息：", e);
        }
        return decryptedData;
    }

    /**
     * 数据加密，算法（DES）
     * @param data 要进行加密的数据
     * @param key
     * @return 加密后的数据
     */
    public static final String encryptDes(String data,
                                          byte[] key) {
        String encryptedData = null;
        try {
            // 加密，并把字节数组编码成字符串
            encryptedData = encryptBASE64(DESCoder.encrypt(data.getBytes(), key));
        } catch (Exception e) {
            throw new RuntimeException("加密错误，错误信息：", e);
        }
        return encryptedData;
    }

    /**
     * RSA签名
     * @param data 原数据
     * @param privateKey
     * @return
     */
    public static final String signRSA(String data,
                                       String privateKey) {
        try {
            return encryptBASE64(RSACoder.sign(data.getBytes(CHARSET), decryptBASE64(privateKey)));
        } catch (Exception e) {
            throw new RuntimeException("签名错误，错误信息：", e);
        }
    }

    /**
     * RSA验签
     * @param data 原数据
     * @param publicKey
     * @param sign
     * @return
     */
    public static final boolean verifyRSA(String data,
                                          String publicKey,
                                          String sign) {
        try {
            return RSACoder.verify(data.getBytes(CHARSET),
                                   decryptBASE64(publicKey),
                                   decryptBASE64(sign));
        } catch (Exception e) {
            throw new RuntimeException("验签错误，错误信息：", e);
        }
    }

    /**
     * 数据加密，算法（RSA）
     * @param data 数据
     * @param privateKey
     * @return 加密后的数据
     */
    public static final String encryptRSAPrivate(String data,
                                                 String privateKey) {
        try {
            return encryptBASE64(RSACoder.encryptByPrivateKey(data.getBytes(CHARSET),
                                                              decryptBASE64(privateKey)));
        } catch (Exception e) {
            throw new RuntimeException("解密错误，错误信息：", e);
        }
    }

    /**
     * 数据解密，算法（RSA）
     * @param cryptData 加密数据
     * @param publicKey
     * @return 解密后的数据
     */
    public static final String decryptRSAPublic(String cryptData,
                                                String publicKey) {
        try {
            // 把字符串解码为字节数组，并解密
            return new String(RSACoder.decryptByPublicKey(decryptBASE64(cryptData),
                                                          decryptBASE64(publicKey)));
        } catch (Exception e) {
            throw new RuntimeException("解密错误，错误信息：", e);
        }
    }

    /**
     * HMAC加密
     * @param type HmacMD2/HmacMD4/HmacMD5/HmacSHA1/HmacSHA224/HmacSHA256/HmacSHA512
     * @return
     */
    public static final String initHmacKey(String type) {
        try {
            return encryptBASE64(HmacCoder.initHmacKey(type));
        } catch (Exception e) {
            throw new RuntimeException("获取HMAC密钥失败：", e);
        }
    }

    /**
     * HMAC加密
     * @param type HmacMD2/HmacMD4/HmacMD5/HmacSHA1/HmacSHA224/HmacSHA256/HmacSHA512
     * @param data
     * @param key initHmacKey
     * @return
     */
    public static final String encryptHMAC(String type,
                                           String data,
                                           String key) {
        try {
            return HmacCoder.encodeHmacHex(type, data.getBytes(CHARSET), decryptBASE64(key));
        } catch (Exception e) {
            throw new RuntimeException("加密错误，错误信息：", e);
        }
    }

    public static String encryptPassword(String password) {
        return encryptMd5(encryptSHA(password));
    }

    public static void main(String[] args) throws Exception {
        System.out.println(encryptDes("SHJR"));
        System.out.println(decryptDes("INzvw/3Qc4q="));
        System.out.println(encryptMd5("SHJR"));
        System.out.println(encryptSHA("1"));
        Map<String, Object> key = RSACoder.initKey();
        String privateKey = encryptBASE64(RSACoder.getPrivateKey(key));
        String publicKey = encryptBASE64(RSACoder.getPublicKey(key));
        System.out.println(privateKey);
        System.out.println(publicKey);
        String sign = signRSA("132", privateKey);
        System.out.println(sign);
        String encrypt = encryptRSAPrivate("132", privateKey);
        System.out.println(encrypt);
        String org = decryptRSAPublic(encrypt, publicKey);
        System.out.println(org);
        System.out.println(verifyRSA(org, publicKey, sign));
        // System.out.println("-------列出加密服务提供者-----");
        // Provider[] pro = Security.getProviders();
        // for (Provider p : pro) {
        // System.out.println("Provider:" + p.getName() + " - version:" +
        // p.getVersion());
        // System.out.println(p.getInfo());
        // }
        // System.out.println("");
        // System.out.println("-------列出系统支持的消息摘要算法：");
        // for (String s : Security.getAlgorithms("MessageDigest")) {
        // System.out.println(s);
        // }
        // System.out.println("-------列出系统支持的生成公钥和私钥对的算法：");
        // for (String s : Security.getAlgorithms("KeyPairGenerator")) {
        // System.out.println(s);
        // }
    }
}
