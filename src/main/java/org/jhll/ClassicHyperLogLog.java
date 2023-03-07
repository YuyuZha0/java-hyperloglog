package org.jhll;

import com.google.common.base.Preconditions;
import com.google.common.hash.Funnel;
import com.google.common.hash.Hashing;
import org.jhll.util.Dense8UIntArray;
import org.jhll.util.Utils;

import java.util.Objects;

/**
 * https://algo.inria.fr/flajolet/Publications/FlFuGaMe07.pdf
 *
 * @param <T>
 */
@SuppressWarnings("UnstableApiUsage")
public final class ClassicHyperLogLog<T> implements HyperLogLog<T, ClassicHyperLogLog<T>> {

  private final Funnel<? super T> funnel;
  private final int log2m;
  private final int registerWidth;
  private final Dense8UIntArray registers;

  /**
   * @param funnel calc hash
   * @param log2m
   *     <p>The log-base-2 of the number of registers used in the HyperLogLog algorithm. Must be at
   *     least 4 and at most 31. This parameter tunes the accuracy of the HyperLogLog structure. The
   *     relative error is given by the expression ±1.04/√(2log2m). Note that increasing {@code
   *     log2m} by 1 doubles the required storage for the hll
   * @param registerWidth
   *     <p>The number of bits used per register in the HyperLogLog algorithm. Must be at least 1
   *     and at most 8. This parameter, in conjunction with {@code log2m}, tunes the maximum
   *     cardinality of the set whose cardinality can be estimated. For clarity, we've provided a
   *     table of registerWidths and {@code log2m}s and the approximate maximum cardinality that can
   *     be estimated with those parameters.
   *     <pre>
   * logm2	registerWidth=1	registerWidth=2	registerWidth=3	registerWidth=4	registerWidth=5	registerWidth=6
   * 10	7.4e+02   128B	3.0e+03   256B	4.7e+04   384B	1.2e+07   512B	7.9e+11   640B	3.4e+21   768B
   * 11	1.5e+03   256B	5.9e+03   512B	9.5e+04   768B	2.4e+07   1.0KB	1.6e+12   1.2KB	6.8e+21   1.5KB
   * 12	3.0e+03   512B	1.2e+04   1.0KB	1.9e+05   1.5KB	4.8e+07   2.0KB	3.2e+12   2.5KB	1.4e+22   3KB
   * 13	5.9e+03   1.0KB	2.4e+04   2.0KB	3.8e+05   3KB	9.7e+07   4KB	6.3e+12   5KB	2.7e+22   6KB
   * 14	1.2e+04   2.0KB	4.7e+04   4KB	7.6e+05   6KB	1.9e+08   8KB	1.3e+13   10KB	5.4e+22   12KB
   * 15	2.4e+04   4KB	9.5e+04   8KB	1.5e+06   12KB	3.9e+08   16KB	2.5e+13   20KB	1.1e+23   24KB
   * 16	4.7e+04   8KB	1.9e+05   16KB	3.0e+06   24KB	7.7e+08   32KB	5.1e+13   40KB	2.2e+23   48KB
   * 17	9.5e+04   16KB	3.8e+05   32KB	6.0e+06   48KB	1.5e+09   64KB	1.0e+14   80KB	4.4e+23   96KB
   * </pre>
   */
  public ClassicHyperLogLog(Funnel<? super T> funnel, int log2m, int registerWidth) {
    Preconditions.checkNotNull(funnel, "null funnel");
    Preconditions.checkArgument(
        log2m >= 4 && log2m <= 31, "illegal log2m(should be within [4, 31]): %s", log2m);
    Preconditions.checkArgument(
        registerWidth >= 1 && registerWidth <= 8,
        "illegal registerWidth(should be within [1, 8]): %s",
        registerWidth);
    this.funnel = funnel;
    this.log2m = log2m;
    this.registerWidth = registerWidth;
    int m = 1 << log2m;
    this.registers = new Dense8UIntArray(m, registerWidth);
  }

  public ClassicHyperLogLog(Funnel<? super T> funnel) {
    this(funnel, 11, 5);
  }

  private static double alpha(int m) {
    switch (m) {
      case 16:
        return 0.673;
      case 32:
        return 0.697;
      case 64:
        return 0.679;
      default:
        return 0.7213 / (1 + 1.079 / m);
    }
  }

  private static double linearCounting(double m, double v) {
    return m * Math.log(m / v);
  }

  private static double fastPowerOf2(int x) {
    return (1L << x);
  }

  private static long round(double d) {
    return Math.round(d);
  }

  private static byte makePrefix(int log2m, int w) {
    assert log2m >= 4 && log2m <= 31;
    assert w >= 1 && w <= 8;
    int n = log2m << 3;
    n |= w;
    return (byte) (n & 0xff);
  }

  public static <U> ClassicHyperLogLog<U> fromByteArray(byte[] bytes, Funnel<? super U> funnel) {
    Preconditions.checkNotNull(bytes, "null bytes");
    Preconditions.checkNotNull(funnel, "null funnel");
    Preconditions.checkArgument(bytes.length > 2, "bytes length is at least 2: %s", bytes.length);
    byte checksum = Utils.checksum(bytes, 0, bytes.length - 1);
    Preconditions.checkArgument(
        checksum == bytes[bytes.length - 1],
        "checksum not match, expected: %s, actual: %s",
        checksum,
        bytes[bytes.length - 1]);
    byte prefix = bytes[0];
    int log2m = (prefix >>> 3) & 0xff;
    int registerWidth = prefix & 0b111;
    ClassicHyperLogLog<U> hyperLogLog = new ClassicHyperLogLog<>(funnel, log2m, registerWidth);
    hyperLogLog.registers.setRawBytes(bytes, 1);
    return hyperLogLog;
  }

  private int rho(long w) {
    return (Long.numberOfLeadingZeros(w) + 1) & Utils.mask32(registerWidth);
  }

  @Override
  public void put(T value) {
    long x = value != null ? Hashing.murmur3_128().hashObject(value, funnel).asLong() : 0L;
    int idx = (int) (x >>> (Long.SIZE - log2m)); // First p bits of x
    long w = x << log2m;

    int v = registers.get(idx);
    int v1 = rho(w);
    if (v1 > v) {
      registers.set(idx, v1);
    }
  }

  @Override
  public long estimatedCardinality() {
    int m = registers.length();
    double x = 0;
    int v = 0;
    for (int i = 0; i < m; ++i) {
      int s = registers.get(i);
      x += 1D / fastPowerOf2(s);
      if (s == 0) {
        ++v;
      }
    }
    double e = alpha(m) * m * m / x;
    if (e <= m * 2.5) {
      if (v == 0) {
        return round(e);
      } else {
        return round(linearCounting(m, v));
      }
    }
    double p32 = fastPowerOf2(32);
    if (e <= p32 / 30D) {
      return round(e);
    }

    return round(-p32 * Math.log(1 - e / p32));
  }

  @Override
  public double relativeError() {
    return 1.04 / Math.sqrt(registers.length());
  }

  @Override
  public byte[] toByteArray() {
    byte[] raw = registers.getRawBytes(false);
    byte[] bytes = new byte[raw.length + 2];
    bytes[0] = makePrefix(log2m, registerWidth);
    System.arraycopy(raw, 0, bytes, 1, raw.length);
    byte checksum = Utils.checksum(bytes, 0, bytes.length - 1);
    bytes[bytes.length - 1] = checksum;
    return bytes;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ClassicHyperLogLog<?> that = (ClassicHyperLogLog<?>) o;
    return log2m == that.log2m
        && registerWidth == that.registerWidth
        && funnel.equals(that.funnel)
        && registers.equals(that.registers);
  }

  @Override
  public int hashCode() {
    return Objects.hash(funnel, log2m, registerWidth, registers);
  }

  @Override
  public ClassicHyperLogLog<T> union(ClassicHyperLogLog<T> other) {
    Preconditions.checkNotNull(other);
    Preconditions.checkArgument(log2m == other.log2m, "log2m not match!");
    ClassicHyperLogLog<T> result =
        new ClassicHyperLogLog<>(funnel, log2m, Math.max(registerWidth, other.registerWidth));
    int length = registers.length();
    for (int i = 0; i < length; ++i) {
      result.registers.set(i, Math.max(registers.get(i), other.registers.get(i)));
    }
    return result;
  }

  @Override
  public void reset() {
    registers.clear();
  }
}
