package org.jhll.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class UtilsTest {

  @Test
  public void testMask() {
    assertEquals(0b1, Utils.mask32(1));
    assertEquals(0b11, Utils.mask32(2));
    assertEquals(0b111, Utils.mask32(3));
    assertEquals(0b1111, Utils.mask32(4));
    assertEquals(0b11111, Utils.mask32(5));
    assertEquals(0b111111, Utils.mask32(6));
    assertEquals(0b1111111, Utils.mask32(7));
    assertEquals(0xff, Utils.mask32(8));
  }
}
