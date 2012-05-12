package org.realityforge.spydle;

import org.realityforge.spydle.runtime.MetricValueSet;

/**
 * The interface invoked when a metric is received by the monitoring system.
 */
public interface MetricHandler
{
  /**
   * Interface via which to receive notification of metrics.
   *
   * @param metrics the set of values to handle.
   * @return true if successfully handled metric, false otherwise.
   */
  boolean handleMetrics( MetricValueSet metrics );
}
