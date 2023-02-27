package org.jhll.hash;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import org.junit.Test;

import java.security.SecureRandom;

import static org.junit.Assert.assertEquals;

public class MurmurHash3Test {

  private static final SecureRandom SECURE_RANDOM = new SecureRandom();

  private static int hash32Guava(byte[] input, HashFunction function) {
    return function.hashBytes(input).asInt();
  }

  private static long hash64Guava(byte[] input, HashFunction function) {
    return function.hashBytes(input).padToLong();
  }

  private static byte[] randomBytes(int len) {
    byte[] bytes = new byte[len];
    SECURE_RANDOM.nextBytes(bytes);
    return bytes;
  }

  @Test
  public void testHash32() {
    byte[] bytes = randomBytes(8);
    assertEquals(hash32Guava(bytes, Hashing.murmur3_32_fixed()), MurmurHash3.hash32x86(bytes));

    bytes = randomBytes(16);
    assertEquals(hash32Guava(bytes, Hashing.murmur3_32_fixed()), MurmurHash3.hash32x86(bytes));

    bytes = randomBytes(32);
    assertEquals(hash32Guava(bytes, Hashing.murmur3_32_fixed()), MurmurHash3.hash32x86(bytes));

    bytes = randomBytes(64);
    assertEquals(hash32Guava(bytes, Hashing.murmur3_32_fixed()), MurmurHash3.hash32x86(bytes));

    bytes = randomBytes(128);
    assertEquals(hash32Guava(bytes, Hashing.murmur3_32_fixed()), MurmurHash3.hash32x86(bytes));
  }

  @Test
  public void testHash64() {
    byte[] bytes = randomBytes(8);
    assertEquals(hash64Guava(bytes, Hashing.murmur3_128()), MurmurHash3.hash128x64(bytes)[0]);

    bytes = randomBytes(16);
    assertEquals(hash64Guava(bytes, Hashing.murmur3_128()), MurmurHash3.hash128x64(bytes)[0]);

    bytes = randomBytes(32);
    assertEquals(hash64Guava(bytes, Hashing.murmur3_128()), MurmurHash3.hash128x64(bytes)[0]);

    bytes = randomBytes(64);
    assertEquals(hash64Guava(bytes, Hashing.murmur3_128()), MurmurHash3.hash128x64(bytes)[0]);

    bytes = randomBytes(128);
    assertEquals(hash64Guava(bytes, Hashing.murmur3_128()), MurmurHash3.hash128x64(bytes)[0]);
  }
}
