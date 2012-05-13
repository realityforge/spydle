package org.realityforge.spydle.runtime.print;

import java.io.PrintStream;
import javax.annotation.Nonnull;
import org.realityforge.spydle.runtime.MetricSink;
import org.realityforge.spydle.runtime.MetricValue;
import org.realityforge.spydle.runtime.MetricValueSet;

/**
 * A simple handler used during debugging that writes to a PrintStream.
 */
public final class PrintStreamMetricSink
  implements MetricSink
{
  private final PrintStream _writer;

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
