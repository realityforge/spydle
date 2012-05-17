package org.realityforge.spydle;

import javax.annotation.Nullable;

/**
 * The interface implemented by components that may generate metric values.
 */
public interface MetricSource
{
  /**
   * @return a set of metrics or null if there was an error.
   */
  @Nullable
  MetricValueSet poll();
}
