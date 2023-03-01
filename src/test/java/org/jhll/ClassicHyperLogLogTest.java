package org.jhll;

import org.jhll.hash.Funnel;
import org.jhll.hash.LongFunnel;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ClassicHyperLogLogTest {

  private static void putLongs(HyperLogLog<Long, ?> hyperLogLog, long fromValue, int num) {
    for (int i = 0; i < num; ++i) {
      hyperLogLog.put(fromValue + i);
    }
  }

  private void verifyRelativeError(int num) {
    ClassicHyperLogLog<Long> hyperLogLog = new ClassicHyperLogLog<>(new LongFunnel());
    putLongs(hyperLogLog, Long.MAX_VALUE >>> 1, num);
    long cardinality = hyperLogLog.estimatedCardinality();
    double err = (cardinality - num) / ((double) num);
    double err0 = hyperLogLog.relativeError();
    System.out.printf("%d/%d: %.3f(%.3f)%n", cardinality, num, err, err0);
    assertTrue(Math.abs(err) < err0);
  }

  private void verifyUnion(int num) {
    ClassicHyperLogLog<Long> hyperLogLog1 = new ClassicHyperLogLog<>(new LongFunnel());
    putLongs(hyperLogLog1, Long.MAX_VALUE >>> 1, num);

    ClassicHyperLogLog<Long> hyperLogLog2 = new ClassicHyperLogLog<>(new LongFunnel());
    putLongs(hyperLogLog2, (Long.MAX_VALUE - num) >>> 1, num);

    ClassicHyperLogLog<Long> hyperLogLog = hyperLogLog1.union(hyperLogLog2);
    long cardinality = hyperLogLog.estimatedCardinality();
    long actual = (Long.MAX_VALUE >>> 1) + num - ((Long.MAX_VALUE - num) >>> 1) + 1;
    double err = (cardinality - actual) / ((double) actual);
    double err0 = hyperLogLog.relativeError();
    System.out.printf("%d/%d: %.3f(%.3f)%n", cardinality, actual, err, err0);
    assertTrue(Math.abs(err) < err0);
  }

  @Test
  public void test() {
    verifyRelativeError(10);
    verifyRelativeError(77);
    verifyRelativeError(1027);
    verifyRelativeError(37659);
    verifyRelativeError(1999999);
  }

  @Test
  public void testUnion() {
    verifyUnion(109);
    verifyUnion(300);
    verifyUnion(5000);
    verifyUnion(730277);
    verifyUnion(2965421);
  }

  @Test
  public void testSerialization() {
    Funnel<Long> funnel = new LongFunnel();
    ClassicHyperLogLog<Long> hyperLogLog = new ClassicHyperLogLog<>(funnel);
    putLongs(hyperLogLog, Long.MAX_VALUE >>> 2, 700000);
    byte[] bytes = hyperLogLog.toByteArray();
    ClassicHyperLogLog<Long> hyperLogLog1 = ClassicHyperLogLog.fromByteArray(bytes, funnel);
    assertEquals(hyperLogLog, hyperLogLog1);
  }
}
