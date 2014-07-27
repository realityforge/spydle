package org.realityforge.spydle.print;

import java.io.PrintStream;
import javax.json.JsonObject;

/**
 * Utility class to interact with the GraphiteSink.
 */
public final class PrintKit
{
  private PrintKit()
  {
  }

  public static PrintStreamMetricSink build( final JsonObject config )
    throws Exception
  {
    final String stream = config.getString( "stream", null );
    final PrintStream printStream = "error".equals( stream ) ? System.err : System.out;
    return new PrintStreamMetricSink( printStream );
  }
}
