package org.realityforge.spydle.runtime;

public final class MetricValue
{
  private final String _key;
  private final long _value;
  private final long _collectedAt;

  public MetricValue( final String key, final long value, final long collectedAt )
  {
    _key = key;
    _value = value;
    _collectedAt = collectedAt;
  }

  public String getKey()
  {
    return _key;
  }

  public long getValue()
  {
    return _value;
  }

  public long getCollectedAt()
  {
    return _collectedAt;
  }
}
