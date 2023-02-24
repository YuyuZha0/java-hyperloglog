package org.jhll;

public interface HyperLogLog<T> {

  void put(T value);

  long estimatedCardinality();

  default double relativeError() {
    return 0;
  }
}
