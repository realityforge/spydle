package org.realityforge.spydle;

import java.util.Collection;

/**
 * A set of metrics collected at a single point in time.
 */
public class MetricValueSet
{
  private final Collection<MetricValue> _metrics;
  private final long _collectedAt;

  public MetricValueSet( final Collection<MetricValue> metrics, final long collectedAt )
  {
    _metrics = metrics;
    _collectedAt = collectedAt;
  }

  public Collection<MetricValue> getMetrics()
  {
    return _metrics;
  }

  public long getCollectedAt()
  {
    return _collectedAt;
  }
}
