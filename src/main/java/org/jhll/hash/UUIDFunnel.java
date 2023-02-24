package org.jhll.hash;

import org.jhll.util.Utils;

import java.util.UUID;

public final class UUIDFunnel implements Funnel<UUID> {

  @Override
  public int hash32(UUID value) {
    if (value != null)
      return MurmurHash3.hash32(value.getMostSignificantBits(), value.getLeastSignificantBits());
    return 0;
  }

  @Override
  public long hash64(UUID value) {
    if (value != null) return value.getMostSignificantBits();
    return 0;
  }

  @Override
  public byte[] toByteArray(UUID value) {
    if (value == null) {
      return Utils.emptyByteArray();
    }
    byte[] b1 = Utils.toByteArray(value.getMostSignificantBits());
    byte[] b2 = Utils.toByteArray(value.getLeastSignificantBits());

    return Utils.concat(b1, b2);
  }
}
