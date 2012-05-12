package org.realityforge.spydle.runtime;

import org.realityforge.spydle.runtime.MetricValueSet;

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
  boolean handleMetrics( MetricValueSet metrics );
}
