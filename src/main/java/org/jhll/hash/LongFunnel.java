package org.jhll.hash;

import com.google.common.hash.Hashing;
import com.google.common.primitives.Longs;
import org.jhll.util.Utils;

public final class LongFunnel implements Funnel<Long> {

  @Override
  public byte[] toByteArray(Long aLong) {
    if (aLong == null) {
      return Utils.emptyByteArray();
    }
    return Longs.toByteArray(aLong);
  }

  @Override
  public long hash64(Long value) {
    if (value != null) {
      return Hashing.murmur3_128().hashLong(value).asLong();
    }
    return 0L;
  }

  @Override
  public int hash32(Long value) {
    if (value != null) {
      return Hashing.murmur3_32_fixed().hashLong(value).asInt();
    }
    return 0;
  }
}
