package org.realityforge.spydle.runtime.jmx;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;

/**
 * Describes a batch of queries against a service invoked with a set delay between queries.
 */
public final class JmxTaskDescriptor
{
  private final JmxServiceDescriptor _service;
  private final List<JmxQuery> _queries;
  private final long _delay;

  public JmxTaskDescriptor( @Nonnull final JmxServiceDescriptor service,
                            @Nonnull final List<JmxQuery> queries,
                            final long delay )
  {
    _service = service;
    _queries = Collections.unmodifiableList( new ArrayList<JmxQuery>( queries ) );
    _delay = delay;
  }

  public JmxServiceDescriptor getService()
  {
    return _service;
  }

  public List<JmxQuery> getQueries()
  {
    return _queries;
  }

  public long getDelay()
  {
    return _delay;
  }
}
