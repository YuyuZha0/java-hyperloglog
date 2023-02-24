package org.jhll.hash;

import org.jhll.util.Utils;

import java.nio.charset.StandardCharsets;

public final class StringFunnel implements Funnel<String> {

  @Override
  public byte[] toByteArray(String value) {
    if (value != null) {
      return value.getBytes(StandardCharsets.UTF_8);
    }
    return Utils.emptyByteArray();
  }
}
