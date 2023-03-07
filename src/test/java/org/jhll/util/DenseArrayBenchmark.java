package org.jhll.util;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@Warmup(iterations = 2, time = 3)
@Measurement(iterations = 2, time = 3)
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
// @Threads(3)
@Fork(1)
public class DenseArrayBenchmark {

  private Dense64UIntArray dense64UIntArray;

  private Dense8UIntArray dense8UIntArray;

  private int index;

  @Setup
  public void setup() {
    int size = 4096;
    int[] values = ThreadLocalRandom.current().ints(size, 0, 0x7f).toArray();
    dense8UIntArray = new Dense8UIntArray(size, 7);
    dense64UIntArray = new Dense64UIntArray(size, 7);
    for (int i = 0; i < values.length; ++i) {
      dense8UIntArray.set(i, values[i]);
      dense64UIntArray.set(i, values[i]);
    }
    index = 0;
  }

  @Benchmark
  public int dense8Bench() {
    if (index >= dense8UIntArray.length() - 1) {
      index = 0;
    }
    int temp = dense8UIntArray.get(index + 1);
    dense8UIntArray.set(index + 1, dense8UIntArray.get(index));
    dense8UIntArray.set(index, temp);
    return dense8UIntArray.get(index + 1);
  }

  @Benchmark
  public int dense64Bench() {
    if (index >= dense64UIntArray.length() - 1) {
      index = 0;
    }
    int temp = dense64UIntArray.get(index + 1);
    dense64UIntArray.set(index + 1, dense64UIntArray.get(index));
    dense64UIntArray.set(index, temp);
    return dense64UIntArray.get(index + 1);
  }
}
