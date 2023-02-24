package org.jhll.hash;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class HashTest {

  @Test
  public void test() {
    HashCode hashCode = Hashing.murmur3_128().hashString("aaaa", StandardCharsets.UTF_8);
    System.out.println(hashCode.asLong());
    System.out.println(
        Arrays.toString(MurmurHash3.hash128x64("aaaa".getBytes(StandardCharsets.UTF_8))));
  }
}
