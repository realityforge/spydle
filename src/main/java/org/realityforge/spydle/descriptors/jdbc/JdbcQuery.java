package org.realityforge.spydle.descriptors.jdbc;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A description of a query to run against a JDBC service.
 */
public class JdbcQuery
{
  private final String _query;
  private final String _namePrefix;

  public JdbcQuery( @Nonnull final String query,
                    @Nullable final String namePrefix )
  {
    _query = query;
    _namePrefix = namePrefix;
  }

  @Nonnull
  public String getQuery()
  {
    return _query;
  }

  @Nullable
  public String getNamePrefix()
  {
    return _namePrefix;
  }

  public String generateKey( final String columnName )
  {
    final String namePrefix = getNamePrefix();
    final StringBuilder sb = new StringBuilder();
    if( null != namePrefix )
    {
      sb.append( namePrefix );
    }
    appendNameComponent( sb, cleanString( columnName ) );
    return sb.toString();
  }

  private void appendNameComponent( final StringBuilder sb, final String value )
  {
    if( 0 != sb.length() )
    {
      sb.append( '.' );
    }
    sb.append( cleanString( value ) );
  }

  private static String cleanString( final String name )
  {
    return name.
      replace( '@', '_' ).
      replace( '.', '_' ).
      replace( '=', '_' ).
      replace( ':', '_' );
  }
}
