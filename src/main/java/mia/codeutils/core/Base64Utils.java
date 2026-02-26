package mia.codeutils.core;

import java.util.Base64;

public final class Base64Utils {
    public static byte[] decodeBase64Bytes(String str) {
        return Base64.getDecoder().decode(str);
    }
    public static String encodeBase64Bytes(byte[] bytes) {
        return new String(Base64.getEncoder().encode(bytes));
    }
}
