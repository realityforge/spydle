package org.realityforge.spydle.jmx;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;

/**
 * Describes a batch of queries against a service invoked with a set delay between queries.
 */
public final class JmxTaskDescriptor
{
  private final JmxConnectionDescriptor _connection;
  private final List<JmxProbeDescriptor> _probes;

  public JmxTaskDescriptor( @Nonnull final JmxConnectionDescriptor connection,
                            @Nonnull final List<JmxProbeDescriptor> probes )
  {
    _connection = connection;
    _probes = Collections.unmodifiableList( new ArrayList<>( probes ) );
  }

  public JmxConnectionDescriptor getConnection()
  {
    return _connection;
  }

  public List<JmxProbeDescriptor> getProbes()
  {
    return _probes;
  }
}
