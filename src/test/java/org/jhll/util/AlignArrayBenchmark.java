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
public class AlignArrayBenchmark {

  private Align64UIntArray align64UIntArray;

  private Align8UIntArray align8UIntArray;

  private int index;

  @Setup
  public void setup() {
    int size = 4096;
    int[] values = ThreadLocalRandom.current().ints(size, 0, 0x7f).toArray();
    align8UIntArray = new Align8UIntArray(size, 7);
    align64UIntArray = new Align64UIntArray(size, 7);
    for (int i = 0; i < values.length; ++i) {
      align8UIntArray.set(i, values[i]);
      align64UIntArray.set(i, values[i]);
    }
    index = 0;
  }

  @Benchmark
  public int dense8Bench() {
    if (index >= align8UIntArray.length() - 1) {
      index = 0;
    }
    int temp = align8UIntArray.get(index + 1);
    align8UIntArray.set(index + 1, align8UIntArray.get(index));
    align8UIntArray.set(index, temp);
    return align8UIntArray.get(index + 1);
  }

  @Benchmark
  public int dense64Bench() {
    if (index >= align64UIntArray.length() - 1) {
      index = 0;
    }
    int temp = align64UIntArray.get(index + 1);
    align64UIntArray.set(index + 1, align64UIntArray.get(index));
    align64UIntArray.set(index, temp);
    return align64UIntArray.get(index + 1);
  }
}
