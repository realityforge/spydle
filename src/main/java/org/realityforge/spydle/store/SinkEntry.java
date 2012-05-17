package org.realityforge.spydle.store;

import javax.annotation.Nonnull;
import org.realityforge.spydle.runtime.MetricSink;

final class SinkEntry
{
  @Nonnull
  private final MetricSink _sink;
  @Nonnull
  private final String _stage;

  SinkEntry( @Nonnull final MetricSink sink, @Nonnull final String stage )
  {
    _sink = sink;
    _stage = stage;
  }

  @Nonnull
  MetricSink getSink()
  {
    return _sink;
  }

  @Nonnull
  String getStage()
  {
    return _stage;
  }
}
