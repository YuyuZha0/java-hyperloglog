package org.jhll.util;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;
import java.util.RandomAccess;

public final class Align8UIntArray implements Serializable, Cloneable, RandomAccess {

  private static final long serialVersionUID = 8779437312286023931L;
  private static final int LOG2_OF_8 = 3;
  private final int length;
  private final int width;
  private final byte[] words;

  public Align8UIntArray(int length, int width) {
    Preconditions.checkArgument(length > 0, "length should > 0: %s", length);
    Preconditions.checkArgument(width > 0 && width <= 8, "width should within [0, 8]: %s", width);
    this.length = length;
    this.width = width;
    this.words = new byte[requiredBytes(length, width)];
  }

  @VisibleForTesting
  public static int requiredBytes(int length, int width) {
    int nBits = length * width;
    int w = nBits >>> LOG2_OF_8;
    return (w << LOG2_OF_8) == nBits ? w : w + 1;
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
    int arrayOffset = fromBits >>> LOG2_OF_8;
    int bitOffset = fromBits - (arrayOffset << LOG2_OF_8);
    int distance = Byte.SIZE - w - bitOffset;
    if (distance >= 0) {
      return (words[arrayOffset] >>> distance) & Utils.mask32(w);
    } else {
      int n = words[arrayOffset] & Utils.mask32(distance + w);
      n <<= -distance;
      n |= (Byte.toUnsignedInt(words[arrayOffset + 1]) >>> (Byte.SIZE + distance));
      return n;
    }
  }

  public void set(int index, int val) {
    Preconditions.checkPositionIndex(index, length);
    checkValue(val);
    int w = width;
    int fromBits = index * w;
    int arrayOffset = fromBits >>> LOG2_OF_8;
    int bitOffset = fromBits - (arrayOffset << LOG2_OF_8);
    int distance = Byte.SIZE - w - bitOffset;
    if (distance >= 0) {
      int e = ~(Utils.mask32(w) << distance);
      words[arrayOffset] = (byte) ((words[arrayOffset] & e) | (val << distance));
    } else {
      int e = ~Utils.mask32(w + distance);
      words[arrayOffset] = (byte) ((words[arrayOffset] & e) | (val >>> -distance));
      int e1 = Utils.mask32(Byte.SIZE + distance);
      words[arrayOffset + 1] =
          (byte) ((words[arrayOffset + 1] & e1) | (val << (Byte.SIZE + distance)));
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
    Arrays.fill(words, zero);
  }

  @Override
  public String toString() {
    return Arrays.toString(toIntArray());
  }

  @SuppressWarnings("MethodDoesntCallSuperMethod")
  @Override
  public Align8UIntArray clone() {
    Align8UIntArray copy = new Align8UIntArray(length, width);
    copy.setWords(words, 0);
    return copy;
  }

  public byte[] getWords(boolean copy) {
    if (copy) {
      return Arrays.copyOf(words, words.length);
    } else {
      return words;
    }
  }

  public void setWords(byte[] src, int offset) {
    Preconditions.checkNotNull(src);
    Preconditions.checkArgument(offset >= 0, "illegal offset: %s", offset);
    Preconditions.checkArgument(src.length >= offset + words.length, "capacity not enough!");
    System.arraycopy(src, offset, words, 0, words.length);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Align8UIntArray array = (Align8UIntArray) o;
    return length == array.length && width == array.width && Arrays.equals(words, array.words);
  }

  @Override
  public int hashCode() {
    int result = Objects.hash(length, width);
    result = 31 * result + Arrays.hashCode(words);
    return result;
  }
}
