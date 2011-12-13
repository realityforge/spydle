package org.realityforge.spydle;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.management.remote.JMXConnector;

/**
 * A descriptor describing how to access a JMX service.
 */
public class ServiceDescriptor
{
  private final String _host;
  private final int _port;
  @Nullable
  private final String _username;
  @Nullable
  private final String _password;

  public ServiceDescriptor( @Nonnull final String host,
                            final int port,
                            @Nullable final String username,
                            @Nullable final String password )
  {
    _host = host;
    _port = port;
    _username = username;
    _password = password;
  }

  public ServiceDescriptor( @Nonnull final String host, final int port )
  {
    this( host, port, null, null );
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

  @Nullable
  public String getUsername()
  {
    return _username;
  }

  @Nullable
  public String getPassword()
  {
    return _password;
  }

  public String getURL()
  {
    return "service:jmx:rmi:///jndi/rmi://" + getHost() + ":" + getPort() + "/jmxrmi";
  }

  public Map<String, ?> getEnvironment()
  {
    final Map<String, String[]> environment = new HashMap<String, String[]>();
    if( null != getUsername() && null != getPassword() )
    {
      environment.put( JMXConnector.CREDENTIALS, new String[]{ getUsername(), getPassword() } );
    }
    return environment;
  }
}
