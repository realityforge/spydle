package org.realityforge.spydle.descriptors.graphite;

import java.net.InetSocketAddress;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Describes a graphite endpoint where data is sent.
 */
public class GraphiteServiceDescriptor
{
  private final InetSocketAddress _socketAddress;
  @Nullable
  private final String _prefix;

  public GraphiteServiceDescriptor( @Nonnull final InetSocketAddress socketAddress,
                                    @Nullable final String prefix )
  {
    _socketAddress = socketAddress;
    _prefix = prefix;
  }

  @Nonnull
  public InetSocketAddress getSocketAddress()
  {
    return _socketAddress;
  }

  @Nullable
  public String getPrefix()
  {
    return _prefix;
  }
}
