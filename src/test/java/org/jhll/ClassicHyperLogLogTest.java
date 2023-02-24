package org.jhll;

import org.jhll.hash.UUIDFunnel;
import org.junit.Test;

import java.util.UUID;

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
}
