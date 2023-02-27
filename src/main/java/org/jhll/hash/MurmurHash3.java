package org.jhll.hash;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Implementation of the MurmurHash3 32-bit and 128-bit hash functions.
 *
 * <p>MurmurHash is a non-cryptographic hash function suitable for general hash-based lookup. The
 * name comes from two basic operations, multiply (MU) and rotate (R), used in its inner loop.
 * Unlike cryptographic hash functions, it is not specifically designed to be difficult to reverse
 * by an adversary, making it unsuitable for cryptographic purposes.
 *
 * <p>This contains a Java port of the 32-bit hash function {@code MurmurHash3_x86_32} and the
 * 128-bit hash function {@code MurmurHash3_x64_128} from Austin Applyby's original {@code c++} code
 * in SMHasher.
 *
 * <p>This is public domain code with no copyrights. From home page of <a
 * href="https://github.com/aappleby/smhasher">SMHasher</a>:
 *
 * <blockquote>
 *
 * "All MurmurHash versions are public domain software, and the author disclaims all copyright to
 * their code."
 *
 * </blockquote>
 *
 * <p>Original adaption from Apache Hive. That adaption contains a {@code hash64} method that is not
 * part of the original MurmurHash3 code. It is not recommended to use these methods. They will be
 * removed in a future release. To obtain a 64-bit hash use half of the bits from the {@code
 * hash128x64} methods using the input data converted to bytes.
 *
 * <p>
 *
 * @see <a href="https://en.wikipedia.org/wiki/MurmurHash">MurmurHash</a>
 * @see <a href="https://github.com/aappleby/smhasher/blob/master/src/MurmurHash3.cpp">Original
 *     MurmurHash3 c++ code</a>
 * @see <a href=
 *     "https://github.com/apache/hive/blob/master/storage-api/src/java/org/apache/hive/common/util/Murmur3.java">
 *     Apache Hive Murmer3</a>
 * @since 1.13
 */
public final class MurmurHash3 {

  /** A default seed to use for the murmur hash algorithm. Has the value {@code 104729}. */
  public static final int DEFAULT_SEED = 104729;

  static final int LONG_BYTES = Long.BYTES;

  // Constants for 32-bit variant
  private static final int C1_32 = 0xcc9e2d51;
  private static final int C2_32 = 0x1b873593;
  private static final int R1_32 = 15;
  private static final int R2_32 = 13;
  private static final int M_32 = 5;
  private static final int N_32 = 0xe6546b64;

  // Constants for 128-bit variant
  private static final long C1 = 0x87c37b91114253d5L;
  private static final long C2 = 0x4cf5ad432745937fL;
  private static final int R1 = 31;
  private static final int R2 = 27;
  private static final int R3 = 33;
  private static final int M = 5;
  private static final int N1 = 0x52dce729;
  private static final int N2 = 0x38495ab5;

  /** No instance methods. */
  private MurmurHash3() {
    throw new IllegalStateException();
  }

  /**
   * Generates 32-bit hash from two longs with a default seed value. This is a helper method that
   * will produce the same result as:
   *
   * <pre>
   * int offset = 0;
   * int seed = 104729;
   * int hash = MurmurHash3.hash32x86(ByteBuffer.allocate(16)
   *                                            .putLong(data1)
   *                                            .putLong(data2)
   *                                            .array(), offset, 16, seed);
   * </pre>
   *
   * @param data1 The first long to hash
   * @param data2 The second long to hash
   * @return The 32-bit hash
   * @see #hash32x86(byte[], int, int, int)
   */
  public static int hash32(final long data1, final long data2) {
    return hash32(data1, data2, DEFAULT_SEED);
  }

  /**
   * Generates 32-bit hash from two longs with the given seed. This is a helper method that will
   * produce the same result as:
   *
   * <pre>
   * int offset = 0;
   * int hash = MurmurHash3.hash32x86(ByteBuffer.allocate(16)
   *                                            .putLong(data1)
   *                                            .putLong(data2)
   *                                            .array(), offset, 16, seed);
   * </pre>
   *
   * @param data1 The first long to hash
   * @param data2 The second long to hash
   * @param seed The initial seed value
   * @return The 32-bit hash
   * @see #hash32x86(byte[], int, int, int)
   */
  public static int hash32(final long data1, final long data2, final int seed) {
    int hash = seed;
    final long r0 = Long.reverseBytes(data1);
    final long r1 = Long.reverseBytes(data2);

    hash = mix32((int) r0, hash);
    hash = mix32((int) (r0 >>> 32), hash);
    hash = mix32((int) (r1), hash);
    hash = mix32((int) (r1 >>> 32), hash);

    hash ^= LONG_BYTES * 2;
    return fmix32(hash);
  }

  /**
   * Generates 32-bit hash from a long with a default seed value. This is a helper method that will
   * produce the same result as:
   *
   * <pre>
   * int offset = 0;
   * int seed = 104729;
   * int hash = MurmurHash3.hash32x86(ByteBuffer.allocate(8)
   *                                            .putLong(data)
   *                                            .array(), offset, 8, seed);
   * </pre>
   *
   * @param data The long to hash
   * @return The 32-bit hash
   * @see #hash32x86(byte[], int, int, int)
   */
  public static int hash32(final long data) {
    return hash32(data, DEFAULT_SEED);
  }

  /**
   * Generates 32-bit hash from a long with the given seed. This is a helper method that will
   * produce the same result as:
   *
   * <pre>
   * int offset = 0;
   * int hash = MurmurHash3.hash32x86(ByteBuffer.allocate(8)
   *                                            .putLong(data)
   *                                            .array(), offset, 8, seed);
   * </pre>
   *
   * @param data The long to hash
   * @param seed The initial seed value
   * @return The 32-bit hash
   * @see #hash32x86(byte[], int, int, int)
   */
  public static int hash32(final long data, final int seed) {
    int hash = seed;
    final long r0 = Long.reverseBytes(data);

    hash = mix32((int) r0, hash);
    hash = mix32((int) (r0 >>> 32), hash);

    hash ^= LONG_BYTES;
    return fmix32(hash);
  }

  /**
   * Generates 32-bit hash from the byte array with a seed of zero. This is a helper method that
   * will produce the same result as:
   *
   * <pre>
   * int offset = 0;
   * int seed = 0;
   * int hash = MurmurHash3.hash32x86(data, offset, data.length, seed);
   * </pre>
   *
   * @param data The input byte array
   * @return The 32-bit hash
   * @see #hash32x86(byte[], int, int, int)
   * @since 1.14
   */
  public static int hash32x86(final byte[] data) {
    return hash32x86(data, 0, data.length, 0);
  }

  /**
   * Generates 32-bit hash from the byte array with the given offset, length and seed.
   *
   * <p>This is an implementation of the 32-bit hash function {@code MurmurHash3_x86_32} from Austin
   * Applyby's original MurmurHash3 {@code c++} code in SMHasher.
   *
   * @param data The input byte array
   * @param offset The offset of data
   * @param length The length of array
   * @param seed The initial seed value
   * @return The 32-bit hash
   * @since 1.14
   */
  public static int hash32x86(
      final byte[] data, final int offset, final int length, final int seed) {
    int hash = seed;
    final int nblocks = length >> 2;

    // body
    for (int i = 0; i < nblocks; i++) {
      final int index = offset + (i << 2);
      final int k = getLittleEndianInt(data, index);
      hash = mix32(k, hash);
    }

    // tail
    final int index = offset + (nblocks << 2);
    int k1 = 0;
    switch (offset + length - index) {
      case 3:
        k1 ^= (data[index + 2] & 0xff) << 16;
      case 2:
        k1 ^= (data[index + 1] & 0xff) << 8;
      case 1:
        k1 ^= (data[index] & 0xff);

        // mix functions
        k1 *= C1_32;
        k1 = Integer.rotateLeft(k1, R1_32);
        k1 *= C2_32;
        hash ^= k1;
    }

    hash ^= length;
    return fmix32(hash);
  }

  /**
   * Generates 128-bit hash from the byte array with a seed of zero. This is a helper method that
   * will produce the same result as:
   *
   * <pre>
   * int offset = 0;
   * int seed = 0;
   * int hash = MurmurHash3.hash128x64(data, offset, data.length, seed);
   * </pre>
   *
   * @param data The input byte array
   * @return The 128-bit hash (2 longs)
   * @see #hash128x64(byte[], int, int, int)
   * @since 1.14
   */
  public static long[] hash128x64(final byte[] data) {
    return hash128x64(data, 0, data.length, 0);
  }

  /**
   * Generates 128-bit hash from the byte array with the given offset, length and seed.
   *
   * <p>This is an implementation of the 128-bit hash function {@code MurmurHash3_x64_128} from
   * Austin Applyby's original MurmurHash3 {@code c++} code in SMHasher.
   *
   * @param data The input byte array
   * @param offset The first element of array
   * @param length The length of array
   * @param seed The initial seed value
   * @return The 128-bit hash (2 longs)
   * @since 1.14
   */
  public static long[] hash128x64(
      final byte[] data, final int offset, final int length, final int seed) {
    // Use an unsigned 32-bit integer as the seed
    return hash128x64Internal(data, offset, length, seed & 0xffffffffL);
  }

  /**
   * Generates 128-bit hash from the byte array with the given offset, length and seed.
   *
   * <p>This is an implementation of the 128-bit hash function {@code MurmurHash3_x64_128} from from
   * Austin Applyby's original MurmurHash3 {@code c++} code in SMHasher.
   *
   * @param data The input byte array
   * @param offset The first element of array
   * @param length The length of array
   * @param seed The initial seed value
   * @return The 128-bit hash (2 longs)
   */
  private static long[] hash128x64Internal(
      final byte[] data, final int offset, final int length, final long seed) {
    long h1 = seed;
    long h2 = seed;
    final int nblocks = length >> 4;

    // body
    for (int i = 0; i < nblocks; i++) {
      final int index = offset + (i << 4);
      long k1 = getLittleEndianLong(data, index);
      long k2 = getLittleEndianLong(data, index + 8);

      // mix functions for k1
      k1 *= C1;
      k1 = Long.rotateLeft(k1, R1);
      k1 *= C2;
      h1 ^= k1;
      h1 = Long.rotateLeft(h1, R2);
      h1 += h2;
      h1 = h1 * M + N1;

      // mix functions for k2
      k2 *= C2;
      k2 = Long.rotateLeft(k2, R3);
      k2 *= C1;
      h2 ^= k2;
      h2 = Long.rotateLeft(h2, R1);
      h2 += h1;
      h2 = h2 * M + N2;
    }

    // tail
    long k1 = 0;
    long k2 = 0;
    final int index = offset + (nblocks << 4);
    switch (offset + length - index) {
      case 15:
        k2 ^= ((long) data[index + 14] & 0xff) << 48;
      case 14:
        k2 ^= ((long) data[index + 13] & 0xff) << 40;
      case 13:
        k2 ^= ((long) data[index + 12] & 0xff) << 32;
      case 12:
        k2 ^= ((long) data[index + 11] & 0xff) << 24;
      case 11:
        k2 ^= ((long) data[index + 10] & 0xff) << 16;
      case 10:
        k2 ^= ((long) data[index + 9] & 0xff) << 8;
      case 9:
        k2 ^= data[index + 8] & 0xff;
        k2 *= C2;
        k2 = Long.rotateLeft(k2, R3);
        k2 *= C1;
        h2 ^= k2;

      case 8:
        k1 ^= ((long) data[index + 7] & 0xff) << 56;
      case 7:
        k1 ^= ((long) data[index + 6] & 0xff) << 48;
      case 6:
        k1 ^= ((long) data[index + 5] & 0xff) << 40;
      case 5:
        k1 ^= ((long) data[index + 4] & 0xff) << 32;
      case 4:
        k1 ^= ((long) data[index + 3] & 0xff) << 24;
      case 3:
        k1 ^= ((long) data[index + 2] & 0xff) << 16;
      case 2:
        k1 ^= ((long) data[index + 1] & 0xff) << 8;
      case 1:
        k1 ^= data[index] & 0xff;
        k1 *= C1;
        k1 = Long.rotateLeft(k1, R1);
        k1 *= C2;
        h1 ^= k1;
    }

    // finalization
    h1 ^= length;
    h2 ^= length;

    h1 += h2;
    h2 += h1;

    h1 = fmix64(h1);
    h2 = fmix64(h2);

    h1 += h2;
    h2 += h1;

    return new long[] {h1, h2};
  }

  /**
   * Gets the little-endian long from 8 bytes starting at the specified index.
   *
   * @param data The data
   * @param index The index
   * @return The little-endian long
   */
  private static long getLittleEndianLong(final byte[] data, final int index) {
    return (((long) data[index] & 0xff))
        | (((long) data[index + 1] & 0xff) << 8)
        | (((long) data[index + 2] & 0xff) << 16)
        | (((long) data[index + 3] & 0xff) << 24)
        | (((long) data[index + 4] & 0xff) << 32)
        | (((long) data[index + 5] & 0xff) << 40)
        | (((long) data[index + 6] & 0xff) << 48)
        | (((long) data[index + 7] & 0xff) << 56);
  }

  /**
   * Gets the little-endian int from 4 bytes starting at the specified index.
   *
   * @param data The data
   * @param index The index
   * @return The little-endian int
   */
  private static int getLittleEndianInt(final byte[] data, final int index) {
    return ((data[index] & 0xff))
        | ((data[index + 1] & 0xff) << 8)
        | ((data[index + 2] & 0xff) << 16)
        | ((data[index + 3] & 0xff) << 24);
  }

  /**
   * Performs the intermediate mix step of the 32-bit hash function {@code MurmurHash3_x86_32}.
   *
   * @param k The data to add to the hash
   * @param hash The current hash
   * @return The new hash
   */
  private static int mix32(int k, int hash) {
    k *= C1_32;
    k = Integer.rotateLeft(k, R1_32);
    k *= C2_32;
    hash ^= k;
    return Integer.rotateLeft(hash, R2_32) * M_32 + N_32;
  }

  /**
   * Performs the final avalanche mix step of the 32-bit hash function {@code MurmurHash3_x86_32}.
   *
   * @param hash The current hash
   * @return The final hash
   */
  private static int fmix32(int hash) {
    hash ^= (hash >>> 16);
    hash *= 0x85ebca6b;
    hash ^= (hash >>> 13);
    hash *= 0xc2b2ae35;
    hash ^= (hash >>> 16);
    return hash;
  }

  /**
   * Performs the final avalanche mix step of the 64-bit hash function {@code MurmurHash3_x64_128}.
   *
   * @param hash The current hash
   * @return The final hash
   */
  private static long fmix64(long hash) {
    hash ^= (hash >>> 33);
    hash *= 0xff51afd7ed558ccdL;
    hash ^= (hash >>> 33);
    hash *= 0xc4ceb9fe1a85ec53L;
    hash ^= (hash >>> 33);
    return hash;
  }
}
