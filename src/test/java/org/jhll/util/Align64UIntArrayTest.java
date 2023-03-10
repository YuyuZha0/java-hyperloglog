package org.jhll.util;

import org.junit.Test;

import java.util.concurrent.ThreadLocalRandom;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class Align64UIntArrayTest {

  @Test
  public void test() {
    Align64UIntArray array = new Align64UIntArray(3, 7);
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

    array = new Align64UIntArray(10, 30);
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

    array = new Align64UIntArray(2, 8);
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
    int[] randomValues = ThreadLocalRandom.current().ints(1000, 0, Integer.MAX_VALUE).toArray();
    Align64UIntArray array = new Align64UIntArray(randomValues.length, 31);
    for (int i = 0; i < randomValues.length; ++i) {
      array.set(i, randomValues[i]);
    }
    assertArrayEquals(randomValues, array.toIntArray());
    //    System.out.println(Arrays.toString(randomValues));
    //    System.out.println(Arrays.toString(array.toIntArray()));
    //    for (int i = 0; i < randomValues.length; ++i) {
    //      assertEquals(
    //          Strings.lenientFormat(
    //              "%s: %s != %s",
    //              i, Integer.toHexString(randomValues[i]), Integer.toHexString(array.get(i))),
    //          randomValues[i],
    //          array.get(i));
    //    }
  }
}
