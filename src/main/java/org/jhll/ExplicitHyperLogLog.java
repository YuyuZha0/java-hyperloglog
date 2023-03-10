package org.jhll;

import com.google.common.base.Preconditions;
import com.google.common.hash.Funnel;
import com.google.common.hash.Hashing;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import gnu.trove.TLongHashSet;
import gnu.trove.TLongIterator;
import org.jhll.util.Utils;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.LongConsumer;

@SuppressWarnings("UnstableApiUsage")
public final class ExplicitHyperLogLog<T> implements HyperLogLog<T> {

  static final byte MARK = (byte) 0xe7;
  private final Funnel<? super T> funnel;
  private final TLongHashSet set;

  public ExplicitHyperLogLog(Funnel<? super T> funnel, int initialCapacity) {
    Preconditions.checkNotNull(funnel, "null funnel");
    Preconditions.checkArgument(
        initialCapacity > 0, "illegal initialCapacity: %s", initialCapacity);
    this.funnel = funnel;
    this.set = new TLongHashSet(initialCapacity);
  }

  public static <U> ExplicitHyperLogLog<U> fromByteArray(byte[] bytes, Funnel<? super U> funnel) {
    Preconditions.checkNotNull(bytes, "null bytes");
    Preconditions.checkNotNull(funnel, "null funnel");
    Preconditions.checkArgument(bytes.length >= 6, "bytes length is at least 6: %s", bytes.length);
    Preconditions.checkArgument(
        bytes[0] == MARK, "first byte should be: %s, but: %s", MARK, bytes[0]);
    byte checksum = Utils.checksum(bytes, 0, bytes.length - 1);
    Preconditions.checkArgument(
        checksum == bytes[bytes.length - 1],
        "checksum not match, expected: %s, actual: %s",
        checksum,
        bytes[bytes.length - 1]);
    int len = Ints.fromByteArray(Arrays.copyOfRange(bytes, 1, 5));
    ExplicitHyperLogLog<U> hyperLogLog = new ExplicitHyperLogLog<>(funnel, len);
    for (int i = 0; i < len; ++i) {
      hyperLogLog.set.add(
          Longs.fromByteArray(Arrays.copyOfRange(bytes, 5 + (i << 3), 13 + (i << 3))));
    }
    return hyperLogLog;
  }

  @Override
  public void put(T value) {
    long x = value != null ? Hashing.murmur3_128().hashObject(value, funnel).asLong() : 0L;
    set.add(x);
  }

  public void forEachValue(LongConsumer consumer) {
    Preconditions.checkNotNull(consumer);
    TLongIterator iterator = set.iterator();
    while (iterator.hasNext()) {
      consumer.accept(iterator.next());
    }
  }

  public long[] toArray() {
    long[] values = set.toArray();
    Arrays.sort(values);
    return values;
  }

  @Override
  public long estimatedCardinality() {
    return set.size();
  }

  @Override
  public byte[] toByteArray() {
    long[] values = toArray();
    int len = serializedSize();
    byte[] output = new byte[len];
    output[0] = MARK;
    System.arraycopy(Ints.toByteArray(values.length), 0, output, 1, 4);
    for (int i = 0; i < values.length; ++i) {
      System.arraycopy(Longs.toByteArray(values[i]), 0, output, 5 + (i << 3), 8);
    }
    byte checksum = Utils.checksum(output, 0, output.length - 1);
    output[output.length - 1] = checksum;
    return output;
  }

  @Override
  public HyperLogLog<T> union(HyperLogLog<T> other) {
    Preconditions.checkNotNull(other);
    if (other instanceof ExplicitHyperLogLog) {
      ExplicitHyperLogLog<T> explicitHyperLogLog = (ExplicitHyperLogLog<T>) other;
      ExplicitHyperLogLog<T> result =
          new ExplicitHyperLogLog<>(funnel, Math.max(set.size(), explicitHyperLogLog.set.size()));
      result.set.addAll(set.toArray());
      result.set.addAll(explicitHyperLogLog.set.toArray());
      return result;
    }
    return other.union(this);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ExplicitHyperLogLog<?> that = (ExplicitHyperLogLog<?>) o;
    return funnel.equals(that.funnel) && set.equals(that.set);
  }

  @Override
  public int hashCode() {
    return Objects.hash(funnel, set);
  }

  @Override
  public int serializedSize() {
    return (set.size() << 3) + 6;
  }

  @Override
  public void reset() {
    set.clear();
  }
}
