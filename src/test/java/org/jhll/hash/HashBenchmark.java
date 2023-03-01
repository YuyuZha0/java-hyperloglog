package org.jhll.hash;

import com.google.common.hash.Hashing;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@Warmup(iterations = 2, time = 3)
@Measurement(iterations = 2, time = 3)
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
// @Threads(3)
@Fork(1)
/**
 *
 *
 * <pre>
 *     Benchmark                            (bytesLen)  Mode  Cnt    Score   Error  Units
 * HashBenchmark.farmHashFingerprint64          32  avgt    2   11.701          ns/op
 * HashBenchmark.farmHashFingerprint64          64  avgt    2   18.520          ns/op
 * HashBenchmark.farmHashFingerprint64         128  avgt    2   43.578          ns/op
 * HashBenchmark.murmur3_128                    32  avgt    2   68.542          ns/op
 * HashBenchmark.murmur3_128                    64  avgt    2   95.080          ns/op
 * HashBenchmark.murmur3_128                   128  avgt    2  110.945          ns/op
 * </pre>
 */
public class HashBenchmark {

  @Param({"32", "64", "128"})
  private int bytesLen;

  private List<byte[]> bytesList;

  private Iterator<byte[]> iterator;

  @Setup
  public void setup() {
    Random random = ThreadLocalRandom.current();
    List<byte[]> bytesList = new ArrayList<>(1000);
    for (int i = 0; i < 1000; ++i) {
      byte[] bytes = new byte[bytesLen];
      random.nextBytes(bytes);
      bytesList.add(bytes);
    }
    this.bytesList = bytesList;
    this.iterator = bytesList.iterator();
  }

  private byte[] nextBytes() {
    if (iterator.hasNext()) {
      return iterator.next();
    }
    iterator = bytesList.iterator();
    return iterator.next();
  }

  @Benchmark
  public long farmHashFingerprint64() {
    return Hashing.farmHashFingerprint64().hashBytes(nextBytes()).asLong();
  }

  @Benchmark
  public long murmur3_128() {
    return Hashing.murmur3_128().hashBytes(nextBytes()).asLong();
  }
}
