package org.realityforge.spydle;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * A simple handler that writes to graphite.
 */
public final class GraphiteMetricHandler
  implements MetricHandler
{
  private final String _prefix;
  private final InetSocketAddress _socketAddress;
  private OutputStream _outputStream;

  public GraphiteMetricHandler( final InetSocketAddress socketAddress,
                                final String prefix )
  {
    _socketAddress = socketAddress;
    _prefix = prefix;
  }

  public void metric( final String key, final long timeInMillis, final long value )
    throws IOException
  {
    final long time = timeInMillis / 1000;
    final StringBuilder sb = new StringBuilder();
    if( null != _prefix )
    {
      sb.append( _prefix );
      if( sb.length() > 0 )
      {
        sb.append( '.' );
      }
    }
    sb.append( key );
    sb.append( ' ' );
    sb.append( value );
    sb.append( ' ' );
    sb.append( time );
    sb.append( '\n' );
    _outputStream.write( sb.toString().getBytes( "US-ASCII" ) );
  }

  public void open()
    throws IOException
  {
    final Socket socket = new Socket();
    socket.connect( _socketAddress );
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
