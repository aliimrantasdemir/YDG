package com.example.lostfound.util;

import java.security.SecureRandom;

public final class OtpUtil {
  private static final SecureRandom RND = new SecureRandom();
  private OtpUtil() {}

  public static String otp6() {
    int x = RND.nextInt(900000) + 100000;
    return String.valueOf(x);
  }
}
