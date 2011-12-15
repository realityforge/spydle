package org.realityforge.spydle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;

/**
 * Describes a batch of queries against a service invoked with a set delay between queries.
 */
public class JobDescriptor
{
  private final ServiceDescriptor _service;
  private final List<QueryDescriptor> _queries;
  private final long _delay;

  public JobDescriptor( @Nonnull final ServiceDescriptor service,
                        @Nonnull final List<QueryDescriptor> queries,
                        final long delay )
  {
    _service = service;
    _queries = Collections.unmodifiableList( new ArrayList<QueryDescriptor>( queries ) );
    _delay = delay;
  }

  public ServiceDescriptor getService()
  {
    return _service;
  }

  public List<QueryDescriptor> getQueries()
  {
    return _queries;
  }

  public long getDelay()
  {
    return _delay;
  }
}
