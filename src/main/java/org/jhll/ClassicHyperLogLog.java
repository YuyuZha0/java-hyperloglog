package org.jhll;

import org.jhll.hash.Funnel;
import org.jhll.util.UnsignedIntArray;
import org.jhll.util.Utils;

import java.util.Objects;

/**
 * https://algo.inria.fr/flajolet/Publications/FlFuGaMe07.pdf
 *
 * @param <T>
 */
public final class ClassicHyperLogLog<T> implements HyperLogLog<T> {

  private final Funnel<? super T> funnel;
  private final int log2m;
  private final int registerWidth;
  private final UnsignedIntArray registers;

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
    Objects.requireNonNull(funnel, "null funnel");
    Utils.checkArgument(
        log2m >= 4 && log2m <= 31, "illegal log2m(should be within [4, 31]): %d", log2m);
    Utils.checkArgument(
        registerWidth >= 1 && registerWidth <= 8,
        "illegal registerWidth(should be within [1, 8]): %d",
        registerWidth);
    this.funnel = funnel;
    this.log2m = log2m;
    this.registerWidth = registerWidth;
    int m = 1 << log2m;
    this.registers = new UnsignedIntArray(m, registerWidth);
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

  private static long ceil(double d) {
    return (long) Math.ceil(d);
  }

  private byte rho(long w) {
    return (byte) ((Long.numberOfLeadingZeros(w) + 1) & Utils.mask(registerWidth));
  }

  @Override
  public void put(T value) {
    long x = funnel.hash64(value);
    // .debugBinary("x", x);
    int idx = (int) (x >>> (Long.SIZE - log2m)); // First p bits of x
    // Utils.debugBinary("idx", (long) idx);
    long w = x << log2m;
    // Utils.debugBinary("w", w);
    // System.out.println(rho(w));

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
        return ceil(e);
      } else {
        return ceil(linearCounting(m, v));
      }
    }
    double p32 = fastPowerOf2(32);
    if (e <= p32 / 30D) {
      return ceil(e);
    }

    return ceil(-p32 * Math.log(1 - e / p32));
  }

  @Override
  public double relativeError() {
    return 1.04 / Math.sqrt(registers.length());
  }
}
