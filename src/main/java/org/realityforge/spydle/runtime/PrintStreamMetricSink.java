package org.realityforge.spydle.runtime;

import java.io.PrintStream;
import javax.annotation.Nonnull;

/**
 * A simple handler used during debugging that writes to a PrintStream.
 */
public final class PrintStreamMetricSink
  implements MetricSink
{
  private final PrintStream _writer;

  public PrintStreamMetricSink()
  {
    this( System.out );
  }

  public PrintStreamMetricSink( @Nonnull final PrintStream writer )
  {
    _writer = writer;
  }

  public boolean handleMetrics( @Nonnull final MetricValueSet metrics )
  {
    for( final MetricValue metric : metrics.getMetrics() )
    {
      _writer.println( metric.getName() + " = " + metric.getValue() );
    }
    return false;
  }
}
