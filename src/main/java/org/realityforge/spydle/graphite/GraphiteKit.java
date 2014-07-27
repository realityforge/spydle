package org.realityforge.spydle.graphite;

import javax.json.JsonObject;

/**
 * Utility class to interact with the GraphiteSink.
 */
public final class GraphiteKit
{
  private GraphiteKit()
  {
  }

  public static GraphiteService build( final JsonObject config )
    throws Exception
  {
    return new GraphiteService( parse( config ) );
  }

  private static GraphiteServiceDescriptor parse( final JsonObject config )
    throws Exception
  {
    final String host = config.getString( "host" );
    final int port = config.getInt( "port" );
    final String prefix = config.getString( "prefix", null );

    return new GraphiteServiceDescriptor( host, port, prefix );
  }
}
