package com.awesomeshot5051;

import java.security.*;

public class AccessKeyGenerator {
    private static final SecureRandom random = new SecureRandom();

    public static String generateAccessKey() {
        StringBuilder key = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            if (i > 0) key.append("-");
            for (int j = 0; j < 4; j++) {
                int n = random.nextInt(16); // 0-15
                key.append(Integer.toHexString(n).toUpperCase());
            }
        }
        return key.toString();
    }

    public static void main(String[] args) {
        System.out.println(generateAccessKey());
    }
}
