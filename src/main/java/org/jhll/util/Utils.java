package org.jhll.util;

import com.google.common.hash.Hashing;

public final class Utils {

  private static final byte[] EMPTY = new byte[0];

  private Utils() {
    throw new IllegalStateException();
  }

  public static byte[] emptyByteArray() {
    return EMPTY;
  }

  public static boolean isNotEmpty(byte[] bytes) {
    return bytes != null && bytes.length > 0;
  }

  public static int mask(int width) {
    return (1 << width) - 1;
  }

  public static byte checksum(byte[] input, int offset, int length) {
    return Hashing.crc32c().hashBytes(input, offset, length).asBytes()[0];
  }
}
