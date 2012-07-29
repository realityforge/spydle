package org.realityforge.spydle;

import javax.annotation.Nonnull;

public final class MetricValue
{
  private final String _name;
  private final Number _value;

  public MetricValue( @Nonnull final String name, @Nonnull final Number value )
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
  public Number getValue()
  {
    return _value;
  }
}
