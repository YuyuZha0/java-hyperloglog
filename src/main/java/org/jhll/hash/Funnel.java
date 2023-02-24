package org.jhll.hash;

import org.jhll.util.Utils;

public interface Funnel<T> {

  default int hash32(T value) {
    byte[] bytes = toByteArray(value);
    if (Utils.isNotEmpty(bytes)) {
      return MurmurHash3.hash32x86(bytes);
    }
    return 0;
  }

  default long hash64(T value) {
    byte[] bytes = toByteArray(value);
    if (Utils.isNotEmpty(bytes)) {
      return MurmurHash3.hash128x64(bytes)[0];
    }
    return 0L;
  }

  byte[] toByteArray(T value);
}
