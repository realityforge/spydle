package org.realityforge.spydle.runtime.graphite;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import javax.annotation.Nonnull;
import org.realityforge.spydle.descriptors.graphite.GraphiteServiceDescriptor;
import org.realityforge.spydle.runtime.MetricValue;

public final class GraphiteService
  implements Closeable
{
  private final GraphiteServiceDescriptor _descriptor;
  private OutputStream _outputStream;
  private Socket _socket;

  public GraphiteService( @Nonnull final GraphiteServiceDescriptor descriptor )
  {
    _descriptor = descriptor;
  }

  public void writeMetric( final MetricValue metric )
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
    sb.append( metric.getName() );
    sb.append( ' ' );
    sb.append( metric.getValue() );
    sb.append( ' ' );
    sb.append( toUnixEpoch( metric.getCollectedAt() ) );
    sb.append( '\n' );
    acquireConnection().write( sb.toString().getBytes( "US-ASCII" ) );
  }

  private OutputStream acquireConnection()
    throws IOException
  {
    if( null != _socket )
    {
      if( _socket.isClosed() )
      {
        close();
      }
      else
      {
        try
        {
          _socket.getOutputStream();
        }
        catch( final IOException ioe )
        {
          close();
        }
      }
    }
    if( null == _socket )
    {
      _socket = new Socket();
      _socket.connect( _descriptor.getSocketAddress() );
    }
    return _socket.getOutputStream();
  }

  public void close()
  {
    if( null != _outputStream )
    {
      try
      {
        _outputStream.close();
      }
      catch( final IOException ioe )
      {
        //Ignored
      }
      _outputStream = null;
    }
  }

  private long toUnixEpoch( final long timeInMillis )
  {
    return timeInMillis / 1000;
  }
}
