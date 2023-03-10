package org.jhll.util;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;
import java.util.RandomAccess;

public final class Align64UIntArray implements Serializable, Cloneable, RandomAccess {

  private static final int LOG2_OF_64 = 6;
  private static final long serialVersionUID = -1651310095059455236L;

  private final int length;
  private final int width;
  private final long[] words;
  private final long mask;

  public Align64UIntArray(int length, int width) {
    Preconditions.checkArgument(length > 0, "length should > 0: %s", length);
    Preconditions.checkArgument(width > 0 && width <= 31, "width should within [0, 31]: %s", width);
    this.length = length;
    this.width = width;
    this.words = new long[requiredLongs(length, width)];
    this.mask = Utils.mask64(width);
  }

  @VisibleForTesting
  public static int requiredLongs(int length, int width) {
    int nBits = length * width;
    int w = nBits >>> LOG2_OF_64;
    return (w << LOG2_OF_64) == nBits ? w : w + 1;
  }

  private int cast(long val) {
    return (int) (val & mask);
  }

  private void checkValue(int value) {
    if (value < 0 || value > mask) {
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
    int arrayOffset = fromBits >>> LOG2_OF_64;
    int bitOffset = fromBits - (arrayOffset << LOG2_OF_64);
    int distance = Long.SIZE - w - bitOffset;
    if (distance >= 0) {
      return cast(words[arrayOffset] >>> distance);
    } else {
      long n = words[arrayOffset] & Utils.mask64(distance + w);
      n <<= -distance;
      n |= (words[arrayOffset + 1] >>> (Long.SIZE + distance));
      return cast(n);
    }
  }

  public void set(int index, int val) {
    Preconditions.checkPositionIndex(index, length);
    checkValue(val);
    int w = width;
    int fromBits = index * w;
    int arrayOffset = fromBits >>> LOG2_OF_64;
    int bitOffset = fromBits - (arrayOffset << LOG2_OF_64);
    int distance = Long.SIZE - w - bitOffset;
    if (distance >= 0) {
      long e = ~(Utils.mask64(w) << distance);
      words[arrayOffset] = ((words[arrayOffset] & e) | ((long) val << distance));
    } else {
      long e = ~Utils.mask64(w + distance);
      words[arrayOffset] = ((words[arrayOffset] & e) | ((long) val >>> -distance));
      long e1 = Utils.mask64(Long.SIZE + distance);
      words[arrayOffset + 1] =
          ((words[arrayOffset + 1] & e1) | ((long) val << (Long.SIZE + distance)));
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
    Arrays.fill(words, 0L);
  }

  @Override
  public String toString() {
    return Arrays.toString(toIntArray());
  }

  @SuppressWarnings("MethodDoesntCallSuperMethod")
  @Override
  public Align64UIntArray clone() {
    Align64UIntArray copy = new Align64UIntArray(length, width);
    copy.setWords(words, 0);
    return copy;
  }

  public long[] getWords(boolean copy) {
    if (copy) {
      return Arrays.copyOf(words, words.length);
    } else {
      return words;
    }
  }

  public void setWords(long[] src, int offset) {
    Preconditions.checkNotNull(src);
    Preconditions.checkArgument(offset >= 0, "illegal offset: %s", offset);
    Preconditions.checkArgument(src.length >= offset + words.length, "capacity not enough!");
    System.arraycopy(src, offset, words, 0, words.length);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Align64UIntArray array = (Align64UIntArray) o;
    return length == array.length && width == array.width && Arrays.equals(words, array.words);
  }

  @Override
  public int hashCode() {
    int result = Objects.hash(length, width);
    result = 31 * result + Arrays.hashCode(words);
    return result;
  }
}
