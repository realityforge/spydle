package org.realityforge.spydle.descriptors.graphite;

import java.net.InetSocketAddress;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Describes a graphite endpoint where data is sent.
 */
public final class GraphiteServiceDescriptor
{
  public static final int DEFAULT_PORT = 2003;

  private final InetSocketAddress _socketAddress;
  @Nonnull
  private final String _host;
  private final int _port;
  @Nullable
  private final String _prefix;

  public GraphiteServiceDescriptor( @Nonnull final String host, 
                                    final int port,
                                    @Nullable final String prefix )
  {
    _host = host;
    _port = port;
    _socketAddress = new InetSocketAddress( host, port );
    _prefix = prefix;
  }

  @Nonnull
  public String getHost()
  {
    return _host;
  }

  public int getPort()
  {
    return _port;
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

  @Override
  public String toString()
  {
    return "Graphite[host=" + getHost() + ",port=" + getPort() + ",prefix=" + getPrefix() + "]";
  }
}
