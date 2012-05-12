package org.realityforge.spydle.runtime;

import javax.annotation.Nonnull;
import org.realityforge.spydle.runtime.MetricSink;
import org.realityforge.spydle.runtime.MetricValueSet;

public final class MulticastMetricSink
  implements MetricSink
{
  private final MetricSink[] _sinks;

  public MulticastMetricSink( @Nonnull final MetricSink[] sinks )
  {
    _sinks = sinks;
  }

  public boolean handleMetrics( @Nonnull final MetricValueSet metrics )
  {
    boolean result = true;
    for( final MetricSink sink : _sinks )
    {
      result &= sink.handleMetrics( metrics );
    }
    return result;
  }
}
