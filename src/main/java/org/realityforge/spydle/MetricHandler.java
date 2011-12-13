package org.realityforge.spydle;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * The interface invoked when a metric is received by the monitoring system.
 */
public interface MetricHandler
{
  void open()
    throws IOException;

  void close()
    throws IOException;

  void metric( String key, long timeInMillis, long value )
    throws IOException;
}
