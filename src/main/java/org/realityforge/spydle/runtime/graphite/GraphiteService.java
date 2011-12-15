package org.realityforge.spydle.runtime.graphite;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import javax.annotation.Nonnull;
import org.realityforge.spydle.descriptors.graphite.GraphiteServiceDescriptor;

public final class GraphiteService
{
  private final GraphiteServiceDescriptor _descriptor;
  private OutputStream _outputStream;
  private Socket _socket;

  public GraphiteService( @Nonnull final GraphiteServiceDescriptor descriptor )
  {
    _descriptor = descriptor;
  }

  public GraphiteServiceDescriptor getDescriptor()
  {
    return _descriptor;
  }

  public OutputStream acquireConnection()
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
}
