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

  public JdbcTaskDescriptor( @Nonnull final JdbcServiceDescriptor service,
                             @Nonnull final List<JdbcQuery> queries )
  {
    _service = service;
    _queries = Collections.unmodifiableList( new ArrayList<>( queries ) );
  }

  public JdbcServiceDescriptor getService()
  {
    return _service;
  }

  public List<JdbcQuery> getQueries()
  {
    return _queries;
  }
}
