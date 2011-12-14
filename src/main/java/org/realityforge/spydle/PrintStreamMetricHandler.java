package org.realityforge.spydle;

import java.io.IOException;
import java.io.PrintStream;
import javax.annotation.Nonnull;

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

  public void open()
    throws IOException
  {
  }

  public void close()
    throws IOException
  {
  }

  public void metric( final String key, final long timeInMillis, final long value )
    throws IOException
  {
    _writer.println( key + " = " + value );
  }
}
