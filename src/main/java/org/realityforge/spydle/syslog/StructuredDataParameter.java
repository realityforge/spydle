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

  @Override
  public boolean equals( final Object o )
  {
    if( this == o )
    {
      return true;
    }
    else if( o == null || getClass() != o.getClass() )
    {
      return false;
    }

    final StructuredDataParameter that = (StructuredDataParameter) o;
    return _name.equals( that._name ) && _value.equals( that._value );
  }

  @Override
  public int hashCode()
  {
    int result = _name.hashCode();
    result = 31 * result + _value.hashCode();
    return result;
  }

  @Override
  public String toString()
  {
    return getName() + "=" + getValue();
  }
}
