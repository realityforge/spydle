package org.realityforge.spydle.runtime.jmx;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.realityforge.spydle.runtime.Namespace;

/**
 * Utility class to interact with the JmxSource.
 */
public final class JmxKit
{
  private JmxKit()
  {
  }

  public static JmxTaskDescriptor parse( final JSONObject config )
    throws Exception
  {
    final String host = getValue( config, "host", String.class );
    final int port = getValue( config, "port", Number.class ).intValue();

    final String username = getValue( config, "username", String.class, false );
    final String password = getValue( config, "password", String.class, false );

    final JmxServiceDescriptor serviceDescriptor = new JmxServiceDescriptor( host, port, username, password );

    final List<JmxQuery> queries = new ArrayList<>();

    final JSONArray queryArray = getValue( config, "queries", JSONArray.class );
    for( final Object queryConfig : queryArray )
    {
      queries.add( parseQuery( (JSONObject) queryConfig ) );
    }

    return new JmxTaskDescriptor( serviceDescriptor, queries );
  }

  private static JmxQuery parseQuery( final JSONObject config )
    throws MalformedObjectNameException
  {
    final String objectName = getValue( config, "object_name", String.class );
    final JSONArray queryArray = getValue( config, "attribute_names", JSONArray.class, false );
    final HashSet<String> attributeNames;
    if( null != queryArray )
    {
      attributeNames = new HashSet<>();
      for( final Object attributeName : queryArray )
      {
        attributeNames.add( attributeName.toString() );
      }
    }
    else
    {
      attributeNames = null;
    }
    final LinkedHashMap<String, String> map = new LinkedHashMap<>();
    final JSONObject namespace = getValue( config, "namespace", JSONObject.class, false );
    if( null != namespace )
    {
      for( final Object key : namespace.keySet() )
      {
        map.put( key.toString(), String.valueOf( namespace.get( key ) ) );
      }
    }
    final ArrayList<String> nameComponents = new ArrayList<>();
    return new JmxQuery( new ObjectName( objectName ), attributeNames, new Namespace( map ), nameComponents );
  }


  /**
   * Retrieve a value from json object with the expected type.
   *
   * @param config the json object.
   * @param key the key to access.
   * @param type the expected type of the value.
   * @param <T> the type of the value
   * @return the value.
   * @throws IllegalArgumentException if value is the incorrect type or the value is missing.
   */
  @Nonnull
  private static <T> T getValue( @Nonnull final JSONObject config,
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
   * @param key the key to access.
   * @param type the expected type of the value.
   * @param ensure true means an exception is raised if the value is missing.
   * @param <T> the type of the value
   * @return the value.
   * @throws IllegalArgumentException if value is the incorrect type or the value is missing and ensure is true.
   */
  @Nullable
  private static <T> T getValue( @Nonnull final JSONObject config,
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
      //noinspection unchecked
      return (T) value;
    }
  }
}
