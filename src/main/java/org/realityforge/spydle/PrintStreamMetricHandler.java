package org.realityforge.spydle;

import java.io.PrintStream;
import javax.annotation.Nonnull;
import org.realityforge.spydle.runtime.MetricValue;
import org.realityforge.spydle.runtime.MetricValueSet;

/**
 * A simple handler used during debugging that writes to a PrintStream.
 */
public class PrintStreamMetricHandler
  implements MetricHandler
{
  private final PrintStream _writer;

  public PrintStreamMetricHandler()
  {
    this( System.out );
  }

  public PrintStreamMetricHandler( @Nonnull final PrintStream writer )
  {
    _writer = writer;
  }

  public void metrics( final MetricValueSet metrics )
  {
    for( final MetricValue metric : metrics.getMetrics() )
    {
      _writer.println( metric.getName() + " = " + metric.getValue() );
    }
  }
}
