package org.realityforge.spydle.util;

import java.util.LinkedHashMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;
import org.json.simple.JSONObject;
import org.realityforge.spydle.Namespace;

/**
 * Utility class for processing json configuration files.
 */
public final class ConfigUtil
{
  private ConfigUtil()
  {
  }

  /**
   * Parse the "namespace" field from the specified configuration.
   * If no namespace defined, return an empty namespace.
   *
   * @param config the configuration.
   * @return the namespace.
   */
  @Nullable
  public static Namespace parseNamespace( final JSONObject config )
  {
    final String namespace = getValue( config, "namespace", String.class, false );
    if( null != namespace )
    {
      try
      {
        final LdapName name = new LdapName( namespace );
        final LinkedHashMap<String, String> map = new LinkedHashMap<>();
        for( final Rdn rdn : name.getRdns() )
        {
          final Attributes attributes = rdn.toAttributes();

          if( attributes.size() != 1 )
          {
            throw new IllegalArgumentException( "Invalid namespace component: " + rdn );
          }
          final String key = attributes.getIDs().next();
          final Attribute attribute = attributes.get( key );
          map.put( key, attribute.get().toString() );
        }
        return new Namespace( map );
      }
      catch( final NamingException ne )
      {
        throw new IllegalArgumentException( "Invalid namespace: " + namespace, ne );
      }
    }
    else
    {
      return null;
    }
  }

  /**
   * Retrieve a value from json object with the expected type.
   *
   * @param config the json object.
   * @param key    the key to access.
   * @param type   the expected type of the value.
   * @param <T>    the type of the value
   * @return the value.
   * @throws IllegalArgumentException if value is the incorrect type or the value is missing.
   */
  @Nonnull
  public static <T> T getValue( @Nonnull final JSONObject config,
                                @Nonnull final String key,
                                @Nonnull final Class<T> type )
  {
    final T value = getValue( config, key, type, true );
    assert null != value;
    return value;
  }

  /**
   * Retrieve a value from json object with the expected type.
   * If ensure parameter is true then the method will raise an exception if the value is missing.
   *
   * @param config the json object.
   * @param key    the key to access.
   * @param type   the expected type of the value.
   * @param ensure true means an exception is raised if the value is missing.
   * @param <T>    the type of the value
   * @return the value.
   * @throws IllegalArgumentException if value is the incorrect type or the value is missing and ensure is true.
   */
  @SuppressWarnings( "unchecked" )
  @Nullable
  public static <T> T getValue( @Nonnull final JSONObject config,
                                @Nonnull final String key,
                                @Nonnull final Class<T> type,
                                final boolean ensure )
  {
    final Object value = config.get( key );
    if( null == value )
    {
      if( ensure )
      {
        throw new IllegalArgumentException( "Missing '" + key + "' parameter from configuration: " + config );
      }
      else
      {
        return null;
      }
    }
    else if( !type.isInstance( value ) )
    {
      throw new IllegalArgumentException( "'" + key + "' parameter from configuration is not of the expected type: " + config );
    }
    else
    {
      return (T) value;
    }
  }
}
