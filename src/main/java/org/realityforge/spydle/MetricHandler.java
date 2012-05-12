package org.realityforge.spydle;

import org.realityforge.spydle.runtime.MetricValueSet;

/**
 * The interface invoked when a metric is received by the monitoring system.
 */
public interface MetricHandler
{
  void metrics( MetricValueSet metric );
}
