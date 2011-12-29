package org.realityforge.spydle.runtime;

import javax.annotation.Nonnull;

public final class MetricValue
{
  private final String _key;
  private final Number _value;
  private final long _collectedAt;

  public MetricValue( final String key, @Nonnull final Number value, final long collectedAt )
  {
    _key = key;
    _value = value;
    _collectedAt = collectedAt;
  }

  public String getKey()
  {
    return _key;
  }

  public Number getValue()
  {
    return _value;
  }

  public long getCollectedAt()
  {
    return _collectedAt;
  }
}
