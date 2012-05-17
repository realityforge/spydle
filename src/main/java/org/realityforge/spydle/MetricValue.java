package org.realityforge.spydle;

import javax.annotation.Nonnull;

public final class MetricValue
{
  private final MetricName _name;
  private final Number _value;

  public MetricValue( @Nonnull final MetricName name, @Nonnull final Number value )
  {
    _name = name;
    _value = value;
  }

  @Nonnull
  public MetricName getName()
  {
    return _name;
  }

  @Nonnull
  public Number getValue()
  {
    return _value;
  }
}
