package org.realityforge.spydle.runtime;

import javax.annotation.Nonnull;

public final class MetricValue
{
  private final MetricName _name;
  private final Number _value;
  private final long _collectedAt;

  public MetricValue( @Nonnull final MetricName name, @Nonnull final Number value, final long collectedAt )
  {
    _name = name;
    _value = value;
    _collectedAt = collectedAt;
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

  public long getCollectedAt()
  {
    return _collectedAt;
  }
}
