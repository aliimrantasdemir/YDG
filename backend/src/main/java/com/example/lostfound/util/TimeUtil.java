package com.example.lostfound.util;

import java.time.Instant;

public final class TimeUtil {
  private TimeUtil() {}
  public static String nowIso() { return Instant.now().toString(); }
}
