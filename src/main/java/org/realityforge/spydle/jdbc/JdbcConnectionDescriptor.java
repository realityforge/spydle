package org.realityforge.spydle.jdbc;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A descriptor describing how to access a JDBC service.
 */
public class JdbcConnectionDescriptor
{
  private final String _jdbcDriver;
  private final String _jdbcURL;
  @Nullable
  private final String _username;
  @Nullable
  private final String _password;

  public JdbcConnectionDescriptor( @Nonnull final String jdbcDriver,
                                   @Nonnull final String jdbcURL,
                                   @Nullable final String username,
                                   @Nullable final String password )
  {
    _jdbcDriver = jdbcDriver;
    _jdbcURL = jdbcURL;
    _username = username;
    _password = password;
  }

  @Nonnull
  public String getJdbcDriver()
  {
    return _jdbcDriver;
  }

  @Nonnull
  public String getJdbcURL()
  {
    return _jdbcURL;
  }

  @Nullable
  public String getUsername()
  {
    return _username;
  }

  @Nullable
  public String getPassword()
  {
    return _password;
  }
}
