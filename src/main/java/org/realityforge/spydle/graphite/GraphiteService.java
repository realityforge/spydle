package org.realityforge.spydle.graphite;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import org.realityforge.spydle.MetricName;
import org.realityforge.spydle.MetricSink;
import org.realityforge.spydle.MetricValue;
import org.realityforge.spydle.MetricValueSet;

public final class GraphiteService
  implements Closeable, MetricSink
{
  private static final Logger LOG = Logger.getLogger( GraphiteService.class.getName() );

  private final GraphiteServiceDescriptor _descriptor;
  private OutputStream _outputStream;
  private Socket _socket;

  public GraphiteService( @Nonnull final GraphiteServiceDescriptor descriptor )
  {
    _descriptor = descriptor;
  }

  @Override
  public boolean handleMetrics( @Nonnull final MetricValueSet metrics )
  {
    final StringBuilder sb = new StringBuilder();
    final String prefix = _descriptor.getPrefix();

    for( final MetricValue metric : metrics.getMetrics() )
    {
      if( null != prefix )
      {
        sb.append( prefix );
        if( sb.length() > 0 )
        {
          sb.append( '.' );
        }
      }

      final MetricName name = metric.getName();
      sb.append( name.getNamespace().toString().replace( '.', '_' ).replace( ',', '.' ) + '.' + name.getKey() );
      sb.append( ' ' );
      sb.append( metric.getValue() );
      sb.append( ' ' );
      sb.append( toUnixEpoch( metrics.getCollectedAt() ) );
      sb.append( '\n' );
    }

    try
    {
      final byte[] bytes = sb.toString().getBytes( "US-ASCII" );
      final OutputStream outputStream = acquireConnection();
      outputStream.write( bytes );
      outputStream.flush();
      return true;
    }
    catch( final UnsupportedEncodingException uee )
    {
      LOG.log( Level.FINE, "Unable to convert message for graphite: " + sb, uee );
      return false;
    }
    catch( final IOException ioe )
    {
      LOG.log( Level.FINE, "Error writing to graphite server: " + _descriptor, ioe );
      close();
      return false;
    }
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
      final Socket socket = new Socket();
      socket.connect( _descriptor.getSocketAddress() );
      _socket = socket;
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
