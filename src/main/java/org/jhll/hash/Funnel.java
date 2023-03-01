package org.jhll.hash;

import com.google.common.hash.Hashing;
import org.jhll.util.Utils;

public interface Funnel<T> {

  default int hash32(T value) {
    byte[] bytes = toByteArray(value);
    if (Utils.isNotEmpty(bytes)) {
      return Hashing.murmur3_32_fixed().hashBytes(bytes).asInt();
    }
    return 0;
  }

  default long hash64(T value) {
    byte[] bytes = toByteArray(value);
    if (Utils.isNotEmpty(bytes)) {
      return Hashing.murmur3_128().hashBytes(bytes).asLong();
    }
    return 0L;
  }

  byte[] toByteArray(T value);
}
