package org.realityforge.spydle.runtime.jdbc;

import java.io.Closeable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import org.realityforge.spydle.descriptors.jdbc.JdbcServiceDescriptor;

public final class JdbcService
  implements Closeable
{
  private final JdbcServiceDescriptor _descriptor;
  private Connection _connection;

  public JdbcService( @Nonnull final JdbcServiceDescriptor descriptor )
  {
    _descriptor = descriptor;
  }

  public Connection acquireConnection()
    throws SQLException
  {
    if( null != _connection && _connection.isClosed() )
    {
      _connection = null;
    }
    if( null == _connection )
    {
      try
      {
        Class.forName( _descriptor.getJdbcDriver() );
      }
      catch( ClassNotFoundException e )
      {
        throw new IllegalStateException( "Missing or invalid JDBC Driver: " + _descriptor.getJdbcDriver() );
      }
      _connection =
        DriverManager.getConnection( _descriptor.getJdbcURL(), _descriptor.getUsername(), _descriptor.getPassword() );
    }
    return _connection;
  }

  public void close()
  {
    if( null != _connection )
    {
      try
      {
        _connection.close();
      }
      catch( final SQLException sqle )
      {
        //Ignored
      }
      _connection = null;
    }
  }
}
