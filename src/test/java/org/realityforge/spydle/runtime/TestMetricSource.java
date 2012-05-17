package org.realityforge.spydle.runtime;

import javax.annotation.Nullable;

/**
 * A metric source used in tests
 */
public class TestMetricSource
  implements MetricSource
{
  @Nullable
  private MetricValueSet _value;

  public TestMetricSource( @Nullable final MetricValueSet value )
  {
    _value = value;
  }

  public TestMetricSource()
  {
    this( null );
  }

  @Override
  public MetricValueSet poll()
  {
    return _value;
  }
}
