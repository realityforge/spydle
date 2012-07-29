package org.realityforge.spydle.jdbc;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.realityforge.spydle.util.ConfigUtil;

/**
 * A description of a query to run against a JDBC service.
 */
public class JdbcProbeDescriptor
{
  private final String _query;
  private final String _keyColumn;
  private final String _namespace;

  public JdbcProbeDescriptor( @Nonnull final String query,
                              @Nullable final String keyColumn,
                              @Nullable final String namespace )
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
  public String getNamespace()
  {
    return _namespace;
  }

  public String generateKey( final String keyValue, final String columnName )
  {
    final StringBuilder sb = new StringBuilder();
    ConfigUtil.appendNameElement( sb, getNamespace() );
    ConfigUtil.appendNameElement( sb, keyValue );
    ConfigUtil.appendNameElement( sb, columnName );
    return sb.toString();
  }
}
