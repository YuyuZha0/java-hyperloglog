package org.jhll.util;

import com.google.common.primitives.Longs;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class UtilsTest {

  @Test
  public void testMask() {
    assertEquals(0b1, Utils.mask(1));
    assertEquals(0b11, Utils.mask(2));
    assertEquals(0b111, Utils.mask(3));
    assertEquals(0b1111, Utils.mask(4));
    assertEquals(0b11111, Utils.mask(5));
    assertEquals(0b111111, Utils.mask(6));
    assertEquals(0b1111111, Utils.mask(7));
    assertEquals(0xff, Utils.mask(8));
  }
}
