package org.realityforge.spydle.print;

import java.io.PrintStream;
import org.json.simple.JSONObject;
import org.realityforge.spydle.util.ConfigUtil;

/**
 * Utility class to interact with the GraphiteSink.
 */
public final class PrintKit
{
  private PrintKit()
  {
  }

  public static PrintStreamMetricSink build( final JSONObject config )
    throws Exception
  {
    final String stream = ConfigUtil.getValue( config, "stream", String.class, false );
    final PrintStream printStream = "error".equals( stream ) ? System.err : System.out;
    return new PrintStreamMetricSink( printStream );
  }
}
