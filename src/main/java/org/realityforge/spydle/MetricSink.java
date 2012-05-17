package org.realityforge.spydle;

import javax.annotation.Nonnull;

/**
 * The interface invoked when a metric is received by the monitoring system.
 */
public interface MetricSink
{
  /**
   * Interface via which to receive notification of metrics.
   *
   * @param metrics the set of values to handle.
   * @return true if successfully handled metric, false otherwise.
   */
  boolean handleMetrics( @Nonnull MetricValueSet metrics );
}
