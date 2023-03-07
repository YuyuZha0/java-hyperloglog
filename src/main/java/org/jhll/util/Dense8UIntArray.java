package org.jhll.util;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;
import java.util.RandomAccess;

public final class Dense8UIntArray implements Serializable, Cloneable, RandomAccess {

  private static final long serialVersionUID = 8779437312286023931L;
  private final int length;
  private final int width;
  private final byte[] raw;

  public Dense8UIntArray(int length, int width) {
    Preconditions.checkArgument(length > 0, "length should > 0: %s", length);
    Preconditions.checkArgument(width > 0 && width <= 8, "width should within [0, 8]: %s", width);
    this.length = length;
    this.width = width;
    this.raw = new byte[minBytesLen(length, width)];
  }

  @VisibleForTesting
  static int minBytesLen(int length, int width) {
    int nBits = length * width;
    int w = nBits >>> 3;
    return (w << 3) == nBits ? w : w + 1;
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
    Preconditions.checkPositionIndex(index, length);
    int w = width;
    int fromBits = index * w;
    int arrayOffset = fromBits >>> 3;
    int bitOffset = fromBits - (arrayOffset << 3);
    int distance = 8 - w - bitOffset;
    if (distance >= 0) {
      return (raw[arrayOffset] >>> distance) & Utils.mask32(w);
    } else {
      int n = raw[arrayOffset] & Utils.mask32(distance + w);
      n <<= -distance;
      n |= (Byte.toUnsignedInt(raw[arrayOffset + 1]) >> (8 + distance));
      return n;
    }
  }

  public void set(int index, int val) {
    Preconditions.checkPositionIndex(index, length);
    checkValue(val);
    int w = width;
    int fromBits = index * w;
    int arrayOffset = fromBits >>> 3;
    int bitOffset = fromBits - (arrayOffset << 3);
    int distance = 8 - w - bitOffset;
    if (distance >= 0) {
      int e = ~(Utils.mask32(w) << distance);
      raw[arrayOffset] = (byte) ((raw[arrayOffset] & e) | (val << distance));
    } else {
      int e = ~Utils.mask32(w + distance);
      raw[arrayOffset] = (byte) ((raw[arrayOffset] & e) | (val >> -distance));
      int e1 = Utils.mask32(8 + distance);
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
  public Dense8UIntArray clone() {
    Dense8UIntArray copy = new Dense8UIntArray(length, width);
    copy.setRawBytes(raw, 0);
    return copy;
  }

  public byte[] getRawBytes(boolean copy) {
    if (copy) {
      return Arrays.copyOf(raw, raw.length);
    } else {
      return raw;
    }
  }

  public void setRawBytes(byte[] src, int offset) {
    Preconditions.checkNotNull(src);
    Preconditions.checkArgument(offset >= 0, "illegal offset: %s", offset);
    Preconditions.checkArgument(src.length >= offset + raw.length, "capacity not enough!");
    System.arraycopy(src, offset, raw, 0, raw.length);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Dense8UIntArray array = (Dense8UIntArray) o;
    return length == array.length && width == array.width && Arrays.equals(raw, array.raw);
  }

  @Override
  public int hashCode() {
    int result = Objects.hash(length, width);
    result = 31 * result + Arrays.hashCode(raw);
    return result;
  }
}
