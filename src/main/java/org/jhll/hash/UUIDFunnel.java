package org.jhll.hash;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Longs;
import org.jhll.util.Utils;

import java.util.UUID;

public final class UUIDFunnel implements Funnel<UUID> {

  @Override
  public int hash32(UUID value) {
    if (value != null) {
      long s = value.getMostSignificantBits();
      return (int) (s ^ (s >>> 32));
    }
    return 0;
  }

  @Override
  public long hash64(UUID value) {
    if (value != null) {
      return value.getMostSignificantBits();
    }
    return 0;
  }

  @Override
  public byte[] toByteArray(UUID value) {
    if (value == null) {
      return Utils.emptyByteArray();
    }
    byte[] b1 = Longs.toByteArray(value.getMostSignificantBits());
    byte[] b2 = Longs.toByteArray(value.getLeastSignificantBits());

    return Bytes.concat(b1, b2);
  }
}
