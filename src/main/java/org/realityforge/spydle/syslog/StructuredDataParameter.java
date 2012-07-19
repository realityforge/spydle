package org.realityforge.spydle.syslog;

import javax.annotation.Nonnull;

public final class StructuredDataParameter
{
  @Nonnull
  private final String _name;
  @Nonnull
  private final String _value;

  public StructuredDataParameter( @Nonnull final String name, @Nonnull final String value )
  {
    _name = name;
    _value = value;
  }

  @Nonnull
  public String getName()
  {
    return _name;
  }

  @Nonnull
  public String getValue()
  {
    return _value;
  }
}
