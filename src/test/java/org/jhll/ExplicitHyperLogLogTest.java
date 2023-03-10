package org.jhll;

import com.google.common.hash.Funnels;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

public class ExplicitHyperLogLogTest {

  @Test
  public void test() {
    ExplicitHyperLogLog<String> hyperLogLog =
        new ExplicitHyperLogLog<>(Funnels.stringFunnel(StandardCharsets.UTF_8), 10);
    hyperLogLog.put("aaa");
    hyperLogLog.put("bbb");
    hyperLogLog.put("ccc");
    assertEquals(3, hyperLogLog.estimatedCardinality());

    ExplicitHyperLogLog<String> hyperLogLog1 =
        new ExplicitHyperLogLog<>(Funnels.stringFunnel(StandardCharsets.UTF_8), 10);
    hyperLogLog1.put("ddd");
    hyperLogLog1.put("bbb");
    hyperLogLog1.put("ccc");
    hyperLogLog1.put("eee");
    assertEquals(4, hyperLogLog1.estimatedCardinality());

    HyperLogLog<String> union1 = hyperLogLog.union(hyperLogLog1);
    assertEquals(5, union1.estimatedCardinality());

    HyperLogLog<String> union2 = hyperLogLog1.union(hyperLogLog);
    assertEquals(union1, union2);
  }

  @Test
  public void testSerialization() {
    ExplicitHyperLogLog<Integer> explicitHyperLogLog =
        new ExplicitHyperLogLog<>(Funnels.integerFunnel(), 100);
    for (int i = 0; i < 100; ++i) {
      explicitHyperLogLog.put(ThreadLocalRandom.current().nextInt());
    }

    byte[] bytes = explicitHyperLogLog.toByteArray();
    assertEquals(explicitHyperLogLog.serializedSize(), bytes.length);

    ExplicitHyperLogLog<Integer> explicitHyperLogLog1 =
        ExplicitHyperLogLog.fromByteArray(bytes, Funnels.integerFunnel());
    assertNotSame(explicitHyperLogLog1, explicitHyperLogLog);
    assertEquals(explicitHyperLogLog1, explicitHyperLogLog);
  }
}
