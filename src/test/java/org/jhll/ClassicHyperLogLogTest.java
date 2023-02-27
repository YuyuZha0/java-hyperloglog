package org.jhll;

import org.jhll.hash.Funnel;
import org.jhll.hash.UUIDFunnel;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.assertEquals;

public class ClassicHyperLogLogTest {

  @Test
  public void test() {
    ClassicHyperLogLog<UUID> hyperLogLog = new ClassicHyperLogLog<>(new UUIDFunnel(), 11, 5);
    for (int i = 0; i < 100000; ++i) {
      hyperLogLog.put(UUID.randomUUID());
    }

    System.out.println(hyperLogLog.estimatedCardinality());
    System.out.println(hyperLogLog.relativeError());
  }

  @Test
  public void testSerialization() {
    Funnel<UUID> funnel = new UUIDFunnel();
    ClassicHyperLogLog<UUID> hyperLogLog = new ClassicHyperLogLog<>(funnel, 11, 5);
    for (int i = 0; i < 10000; ++i) {
      hyperLogLog.put(UUID.randomUUID());
    }
    byte[] bytes = hyperLogLog.toByteArray();
    ClassicHyperLogLog<UUID> hyperLogLog1 = ClassicHyperLogLog.fromByteArray(bytes, funnel);
    assertEquals(hyperLogLog, hyperLogLog1);
  }
}
