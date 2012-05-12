package org.realityforge.spydle;

import java.io.IOException;
import org.realityforge.spydle.runtime.MetricValue;
import org.realityforge.spydle.runtime.MetricValueSet;

/**
 * The interface invoked when a metric is received by the monitoring system.
 */
public interface MetricHandler
{
  void metrics( MetricValueSet metric )
    throws IOException;
}
