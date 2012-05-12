package org.realityforge.spydle.runtime.jdbc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;

/**
 * Describes a batch of queries against a service invoked with a set delay between queries.
 */
public class JdbcTaskDescriptor
{
  private final JdbcServiceDescriptor _service;
  private final List<JdbcQuery> _queries;
  private final long _delay;

  public JdbcTaskDescriptor( @Nonnull final JdbcServiceDescriptor service,
                             @Nonnull final List<JdbcQuery> queries,
                             final long delay )
  {
    _service = service;
    _queries = Collections.unmodifiableList( new ArrayList<JdbcQuery>( queries ) );
    _delay = delay;
  }

  public JdbcServiceDescriptor getService()
  {
    return _service;
  }

  public List<JdbcQuery> getQueries()
  {
    return _queries;
  }

  public long getDelay()
  {
    return _delay;
  }
}
