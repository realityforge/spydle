package org.realityforge.spydle.graphite;

import org.json.simple.JSONObject;
import org.realityforge.spydle.util.ConfigUtil;

/**
 * Utility class to interact with the GraphiteSink.
 */
public final class GraphiteKit
{
  private GraphiteKit()
  {
  }

  public static GraphiteService build( final JSONObject config )
    throws Exception
  {
    return new GraphiteService( parse( config ) );
  }

  private static GraphiteServiceDescriptor parse( final JSONObject config )
    throws Exception
  {
    final String host = ConfigUtil.getValue( config, "host", String.class );
    final int port = ConfigUtil.getValue( config, "port", Number.class ).intValue();
    final String prefix = ConfigUtil.getValue( config, "prefix", String.class, false );

    return new GraphiteServiceDescriptor( host, port, prefix );
  }
}
