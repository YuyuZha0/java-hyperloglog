package org.jhll;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;

public interface HyperLogLog<T> {

  void put(T value);

  long estimatedCardinality();

  default double relativeError() {
    return 0;
  }

  byte[] toByteArray();

  default void writeTo(OutputStream out) throws IOException {
    Objects.requireNonNull(out, "null outputStream");
    byte[] bytes = Objects.requireNonNull(toByteArray(), "null bytes");
    if (bytes.length > 0) {
      out.write(bytes);
    }
  }
}
