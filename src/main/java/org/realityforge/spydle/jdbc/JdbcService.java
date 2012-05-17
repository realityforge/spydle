package org.realityforge.spydle.jdbc;

import java.io.Closeable;
import java.io.EOFException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import org.realityforge.spydle.MetricSource;
import org.realityforge.spydle.MetricValue;
import org.realityforge.spydle.MetricValueSet;

public final class JdbcService
  implements Closeable, MetricSource
{
  private static final Logger LOG = Logger.getLogger( JdbcService.class.getName() );

  private final JdbcTaskDescriptor _descriptor;
  private Connection _connection;
  private boolean _closed;

  public JdbcService( @Nonnull final JdbcTaskDescriptor descriptor )
  {
    _descriptor = descriptor;
  }

  private Connection acquireConnection()
    throws IOException, SQLException
  {
    if( _closed )
    {
      throw new EOFException();
    }
    if( null != _connection && _connection.isClosed() )
    {
      _connection = null;
    }
    if( null == _connection )
    {
      try
      {
        Class.forName( _descriptor.getConnection().getJdbcDriver() );
      }
      catch( ClassNotFoundException e )
      {
        throw new IllegalStateException( "Missing or invalid JDBC Driver: " + _descriptor.getConnection().getJdbcDriver() );
      }
      _connection =
        DriverManager.getConnection( _descriptor.getConnection().getJdbcURL(),
                                     _descriptor.getConnection().getUsername(),
                                     _descriptor.getConnection().getPassword() );
    }
    return _connection;
  }

  public void close()
  {
    _closed = true;
    doClose();
  }

  private void doClose()
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

  @Override
  public MetricValueSet poll()
  {
    final LinkedList<MetricValue> metrics = new LinkedList<>();
    for( final JdbcProbeDescriptor probe : _descriptor.getProbes() )
    {
      try
      {
        collectJdbcQueryResults( metrics, acquireConnection(), probe );
      }
      catch( final Exception e )
      {
        LOG.log( Level.FINE, "Error querying MBeanServer: " + _descriptor.getConnection() + " Query: " + probe, e );
        doClose();
        return null;
      }
    }

    return new MetricValueSet( metrics, System.currentTimeMillis() );
  }


  private void collectJdbcQueryResults( final LinkedList<MetricValue> metrics,
                                        final Connection connection,
                                        final JdbcProbeDescriptor probe )
    throws SQLException, IOException
  {
    //TODO: Gracefully handle SQL exceptions and close resources correctly
    final Statement statement = connection.createStatement();
    final ResultSet resultSet = statement.executeQuery( probe.getQuery() );
    final HashMap<String, Integer> columns = new HashMap<>();
    final ResultSetMetaData metaData = resultSet.getMetaData();
    final int columnCount = metaData.getColumnCount();
    for( int i = 1; i <= columnCount; i++ )
    {
      final int columnType = metaData.getColumnType( i );
      if( Types.TINYINT == columnType ||
          Types.DECIMAL == columnType ||
          Types.DOUBLE == columnType ||
          Types.FLOAT == columnType ||
          Types.INTEGER == columnType ||
          Types.SMALLINT == columnType ||
          Types.NUMERIC == columnType ||
          Types.BIGINT == columnType )
      {
        columns.put( metaData.getColumnName( i ), i );
      }
    }
    while( resultSet.next() )
    {
      final String key;
      if( null != probe.getKeyColumn() )
      {
        key = resultSet.getObject( probe.getKeyColumn() ).toString();
      }
      else
      {
        key = null;
      }
      for( final Map.Entry<String, Integer> entry : columns.entrySet() )
      {
        final String columnName = entry.getKey();
        final Object value = resultSet.getObject( columnName );
        metrics.add( new MetricValue( probe.generateKey( key, columnName ), (Number) value ) );
      }
    }
    resultSet.close();
    statement.close();
  }
}
