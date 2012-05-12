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
  private final JdbcConnectionDescriptor _connection;
  private final List<JdbcProbeDescriptor> _probes;

  public JdbcTaskDescriptor( @Nonnull final JdbcConnectionDescriptor connection,
                             @Nonnull final List<JdbcProbeDescriptor> probes )
  {
    _connection = connection;
    _probes = Collections.unmodifiableList( new ArrayList<>( probes ) );
  }

  public JdbcConnectionDescriptor getConnection()
  {
    return _connection;
  }

  public List<JdbcProbeDescriptor> getProbes()
  {
    return _probes;
  }
}
