package org.realityforge.spydle;

import java.io.IOException;
import org.realityforge.spydle.runtime.MetricValue;

/**
 * The interface invoked when a metric is received by the monitoring system.
 */
public interface MetricHandler
{
  void metric( MetricValue metric )
    throws IOException;
}
