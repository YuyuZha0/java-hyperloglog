package org.jhll.util;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

public final class UnsignedIntArray implements Serializable, Cloneable {

  private static final long serialVersionUID = 8779437312286023931L;
  private final int length;
  private final int width;
  private final byte[] raw;

  public UnsignedIntArray(int length, int width) {
    Utils.checkArgument(length > 0, "length should > 0: %d", length);
    Utils.checkArgument(width > 0 && width <= 8, "width should within [0, 8]: %d", width);
    this.length = length;
    this.width = width;
    this.raw = new byte[minBytesLen(length, width)];
  }

  static int minBytesLen(int length, int width) {
    int nBits = length * width;
    int w = nBits >>> 3;
    return (w << 3) == nBits ? w : w + 1;
  }

  private void checkIndex(int index) {
    if (index < 0 || index >= length) {
      throw new ArrayIndexOutOfBoundsException(index);
    }
  }

  private void checkValue(int value) {
    if (value < 0 || value >= (1 << width)) {
      throw new ArrayStoreException("Int value overflow: " + value);
    }
  }

  public int length() {
    return length;
  }

  public int width() {
    return width;
  }

  public int get(int index) {
    checkIndex(index);
    int w = width;
    int fromBits = index * w;
    int arrayOffset = fromBits >>> 3;
    int bitOffset = fromBits - (arrayOffset << 3);
    int distance = 8 - w - bitOffset;
    if (distance >= 0) {
      return (raw[arrayOffset] >>> distance) & Utils.mask(w);
    } else {
      int n = raw[arrayOffset] & Utils.mask(distance + w);
      n <<= -distance;
      n |= (Byte.toUnsignedInt(raw[arrayOffset + 1]) >> (8 + distance));
      return n;
    }
  }

  public void set(int index, int val) {
    checkIndex(index);
    checkValue(val);
    int w = width;
    int fromBits = index * w;
    int arrayOffset = fromBits >>> 3;
    int bitOffset = fromBits - (arrayOffset << 3);
    int distance = 8 - w - bitOffset;
    if (distance >= 0) {
      int e = ~(Utils.mask(w) << distance);
      raw[arrayOffset] = (byte) ((raw[arrayOffset] & e) | (val << distance));
    } else {
      int e = ~Utils.mask(w + distance);
      raw[arrayOffset] = (byte) ((raw[arrayOffset] & e) | (val >> -distance));
      int e1 = Utils.mask(8 + distance);
      raw[arrayOffset + 1] = (byte) ((raw[arrayOffset + 1] & e1) | (val << (8 + distance)));
    }
  }

  public int[] toIntArray() {
    int[] a = new int[length];
    for (int i = 0; i < a.length; ++i) {
      a[i] = get(i);
    }
    return a;
  }

  public void clear() {
    byte zero = 0;
    Arrays.fill(raw, zero);
  }

  @Override
  public String toString() {
    return Arrays.toString(toIntArray());
  }

  @SuppressWarnings("MethodDoesntCallSuperMethod")
  @Override
  public UnsignedIntArray clone() {
    UnsignedIntArray copy = new UnsignedIntArray(length, width);
    copy.setRawBytes(raw, 0);
    return copy;
  }

  public void getRawBytes(byte[] dest, int offset) {
    Objects.requireNonNull(dest);
    Utils.checkArgument(offset >= 0, "illegal offset: %d", offset);
    Utils.checkArgument(dest.length >= offset + raw.length, "capacity not enough!");
    System.arraycopy(raw, 0, dest, offset, raw.length);
  }

  public void setRawBytes(byte[] src, int offset) {
    Objects.requireNonNull(src);
    Utils.checkArgument(offset >= 0, "illegal offset: %d", offset);
    Utils.checkArgument(src.length >= offset + raw.length, "capacity not enough!");
    System.arraycopy(src, offset, raw, 0, raw.length);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    UnsignedIntArray array = (UnsignedIntArray) o;
    return length == array.length && width == array.width && Arrays.equals(raw, array.raw);
  }

  @Override
  public int hashCode() {
    int result = Objects.hash(length, width);
    result = 31 * result + Arrays.hashCode(raw);
    return result;
  }
}
