package org.realityforge.spydle;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import javax.annotation.Nonnull;
import org.realityforge.spydle.descriptors.graphite.GraphiteServiceDescriptor;

/**
 * A simple handler that writes to graphite.
 */
public final class GraphiteMetricHandler
  implements MetricHandler
{
  private final GraphiteServiceDescriptor _descriptor;
  private OutputStream _outputStream;

  public GraphiteMetricHandler( @Nonnull final GraphiteServiceDescriptor descriptor )
  {
    _descriptor = descriptor;
  }

  public void metric( final String key, final long timeInMillis, final long value )
    throws IOException
  {
    final StringBuilder sb = new StringBuilder();
    final String prefix = _descriptor.getPrefix();
    if( null != prefix )
    {
      sb.append( prefix );
      if( sb.length() > 0 )
      {
        sb.append( '.' );
      }
    }
    sb.append( key );
    sb.append( ' ' );
    sb.append( value );
    sb.append( ' ' );
    sb.append( toUnixEpoch( timeInMillis ) );
    sb.append( '\n' );
    _outputStream.write( sb.toString().getBytes( "US-ASCII" ) );
  }

  private long toUnixEpoch( final long timeInMillis )
  {
    return timeInMillis / 1000;
  }

  public void open()
    throws IOException
  {
    final Socket socket = new Socket();
    socket.connect( _descriptor.getSocketAddress() );
    _outputStream = socket.getOutputStream();
  }

  public void close()
    throws IOException
  {
    if( null != _outputStream )
    {
      _outputStream.close();
      _outputStream = null;
    }
  }
}
