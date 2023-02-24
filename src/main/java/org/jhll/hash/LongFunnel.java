package org.jhll.hash;

import org.jhll.util.Utils;

public final class LongFunnel implements Funnel<Long> {

  @Override
  public byte[] toByteArray(Long aLong) {
    if (aLong == null) {
      return Utils.emptyByteArray();
    }
    return Utils.toByteArray(aLong);
  }

  @Override
  public long hash64(Long value) {
    return value != null ? value : 0L;
  }

  @Override
  public int hash32(Long value) {
    return value != null ? MurmurHash3.hash32(value) : 0;
  }
}
