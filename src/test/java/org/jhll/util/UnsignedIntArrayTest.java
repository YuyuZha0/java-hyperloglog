package org.jhll.util;

import org.junit.Test;

import java.util.concurrent.ThreadLocalRandom;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class UnsignedIntArrayTest {

  @Test
  public void testMinBytesLen() {
    assertEquals(4, UnsignedIntArray.minBytesLen(4, 7));
    assertEquals(1, UnsignedIntArray.minBytesLen(4, 2));
    assertEquals(10, UnsignedIntArray.minBytesLen(16, 5));
    assertEquals(10, UnsignedIntArray.minBytesLen(15, 5));
  }

  @Test
  public void test() {
    UnsignedIntArray array = new UnsignedIntArray(3, 7);
    array.set(0, 99);
    assertEquals(99, array.get(0));
    array.set(1, 127);
    assertEquals(99, array.get(0));
    assertEquals(127, array.get(1));
    array.set(2, 111);
    assertEquals(99, array.get(0));
    assertEquals(127, array.get(1));
    assertEquals(111, array.get(2));
    array.clear();
    assertEquals(0, array.get(0));
    assertEquals(0, array.get(1));
    assertEquals(0, array.get(2));

    array = new UnsignedIntArray(10, 4);
    array.set(0, 15);
    assertEquals(15, array.get(0));
    array.set(7, 9);
    assertEquals(15, array.get(0));
    assertEquals(9, array.get(7));
    array.set(3, 13);
    assertEquals(15, array.get(0));
    assertEquals(9, array.get(7));
    assertEquals(13, array.get(3));
    array.clear();
    assertEquals(0, array.get(0));
    assertEquals(0, array.get(1));
    assertEquals(0, array.get(2));

    array = new UnsignedIntArray(2, 8);
    array.set(0, 255);
    assertEquals(255, array.get(0));
    array.set(1, 255);
    assertEquals(255, array.get(0));
    assertEquals(255, array.get(1));
    array.clear();
    assertEquals(0, array.get(0));
    assertEquals(0, array.get(1));
  }

  @Test
  public void testRandom() {
    int[] randomValues = ThreadLocalRandom.current().ints(1000, 0, 32).toArray();
    UnsignedIntArray array = new UnsignedIntArray(randomValues.length, 5);
    for (int i = 0; i < randomValues.length; ++i) {
      array.set(i, randomValues[i]);
    }
    assertArrayEquals(randomValues, array.toIntArray());
    assertArrayEquals(randomValues, array.clone().toIntArray());
  }
}
