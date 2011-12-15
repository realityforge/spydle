package org.realityforge.spydle.descriptors.jmx;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;

/**
 * Describes a batch of queries against a service invoked with a set delay between queries.
 */
public class JmxTaskDescriptor
{
  private final JmxServiceDescriptor _service;
  private final List<Query> _queries;
  private final long _delay;

  public JmxTaskDescriptor( @Nonnull final JmxServiceDescriptor service,
                            @Nonnull final List<Query> queries,
                            final long delay )
  {
    _service = service;
    _queries = Collections.unmodifiableList( new ArrayList<Query>( queries ) );
    _delay = delay;
  }

  public JmxServiceDescriptor getService()
  {
    return _service;
  }

  public List<Query> getQueries()
  {
    return _queries;
  }

  public long getDelay()
  {
    return _delay;
  }
}
