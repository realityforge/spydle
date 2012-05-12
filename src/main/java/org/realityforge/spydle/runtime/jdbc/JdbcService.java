package org.realityforge.spydle.runtime.jdbc;

import java.io.Closeable;
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
import org.realityforge.spydle.runtime.MetricSource;
import org.realityforge.spydle.runtime.MetricValue;
import org.realityforge.spydle.runtime.MetricValueSet;

public final class JdbcService
  implements Closeable, MetricSource
{
  private static final Logger LOG = Logger.getLogger( JdbcService.class.getName() );

  private final JdbcTaskDescriptor _descriptor;
  private Connection _connection;

  public JdbcService( @Nonnull final JdbcTaskDescriptor descriptor )
  {
    _descriptor = descriptor;
  }

  private Connection acquireConnection()
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
        Class.forName( _descriptor.getService().getJdbcDriver() );
      }
      catch( ClassNotFoundException e )
      {
        throw new IllegalStateException( "Missing or invalid JDBC Driver: " + _descriptor.getService().getJdbcDriver() );
      }
      _connection =
        DriverManager.getConnection( _descriptor.getService().getJdbcURL(),
                                     _descriptor.getService().getUsername(),
                                     _descriptor.getService().getPassword() );
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

  @Override
  public MetricValueSet poll()
  {
    final LinkedList<MetricValue> metrics = new LinkedList<>();
    for( final JdbcQuery query : _descriptor.getQueries() )
    {
      try
      {
        collectJdbcQueryResults( metrics, acquireConnection(), query );
      }
      catch( final Exception e )
      {
        LOG.log( Level.FINE, "Error querying MBeanServer: " + _descriptor.getService() + " Query: " + query, e );
        close();
        return null;
      }
    }

    return new MetricValueSet( metrics, System.currentTimeMillis() );
  }


  private void collectJdbcQueryResults( final LinkedList<MetricValue> metrics,
                                        final Connection connection,
                                        final JdbcQuery query )
    throws SQLException, IOException
  {
    final Statement statement = connection.createStatement();
    final ResultSet resultSet = statement.executeQuery( query.getQuery() );
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
      if( null != query.getKeyColumn() )
      {
        key = resultSet.getObject( query.getKeyColumn() ).toString();
      }
      else
      {
        key = null;
      }
      for( final Map.Entry<String, Integer> entry : columns.entrySet() )
      {
        final String columnName = entry.getKey();
        final Object value = resultSet.getObject( columnName );
        metrics.add( new MetricValue( query.generateKey( key, entry.getKey() ), (Number) value ) );
      }
    }
    resultSet.close();
    statement.close();
  }
}
