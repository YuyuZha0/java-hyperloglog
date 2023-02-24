package org.jhll.util;

public final class Utils {

  private static final byte[] EMPTY = new byte[0];

  private Utils() {
    throw new IllegalStateException();
  }

  public static void debugBinary(String name, int n) {
    System.out.printf("%4s:#%32s%n", name, Integer.toBinaryString(n));
  }

  public static void debugBinary(String name, long n) {
    System.out.printf("%4s:#%64s%n", name, Long.toBinaryString(n));
  }

  public static byte[] emptyByteArray() {
    return EMPTY;
  }

  public static void checkArgument(boolean cond, String template, Object... args) {
    if (!cond) {
      String msg = (args == null || args.length == 0) ? template : String.format(template, args);
      throw new IllegalArgumentException(msg);
    }
  }

  public static boolean isNotEmpty(byte[] bytes) {
    return bytes != null && bytes.length > 0;
  }

  public static int mask(int width) {
    return (1 << width) - 1;
  }

  public static byte[] toByteArray(long value) {
    byte[] result = new byte[8];
    for (int i = 7; i >= 0; i--) {
      result[i] = (byte) (value & 0xffL);
      value >>= 8;
    }
    return result;
  }

  public static byte[] concat(byte[] b1, byte[] b2) {
    byte[] b = new byte[b1.length + b2.length];
    System.arraycopy(b1, 0, b, 0, b1.length);
    System.arraycopy(b2, 0, b, b1.length, b2.length);
    return b;
  }
}
