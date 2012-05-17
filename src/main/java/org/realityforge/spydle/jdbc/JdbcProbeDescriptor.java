package org.realityforge.spydle.jdbc;

import java.util.LinkedHashMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.realityforge.spydle.MetricName;
import org.realityforge.spydle.Namespace;

/**
 * A description of a query to run against a JDBC service.
 */
public class JdbcProbeDescriptor
{
  private final String _query;
  private final String _keyColumn;
  private final Namespace _namespace;

  public JdbcProbeDescriptor( @Nonnull final String query,
                              @Nullable final String keyColumn,
                              @Nullable final Namespace namespace )
  {
    _query = query;
    _keyColumn = keyColumn;
    _namespace = namespace;
  }

  @Nonnull
  public String getQuery()
  {
    return _query;
  }

  @Nullable
  public String getKeyColumn()
  {
    return _keyColumn;
  }

  @Nullable
  public Namespace getNamespace()
  {
    return _namespace;
  }

  public MetricName generateKey( final String keyValue, final String columnName )
  {
    final LinkedHashMap<String, String> map = new LinkedHashMap<>();
    final Namespace namePrefix = getNamespace();
    if( null != namePrefix )
    {
      map.putAll( namePrefix.getNameComponents() );
    }
    if( null != keyValue )
    {
      map.put( _keyColumn, keyValue );
    }
    return new MetricName( new Namespace( map ), columnName );
  }
}
