package org.realityforge.spydle;

import java.io.IOException;

/**
 * The interface invoked when a metric is received by the monitoring system.
 */
public interface MetricHandler
{
  void metric( String key, long timeInMillis, long value )
    throws IOException;
}
