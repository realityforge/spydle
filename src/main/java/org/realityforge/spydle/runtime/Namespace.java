package org.realityforge.spydle.runtime;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.annotation.Nonnull;

/**
 * A representation of a namespace consisting of a set of ordered key-value pairs. The representation is immutable.
 */
public final class Namespace
{
  private final Map<String, String> _nameComponents;
  private String _materialized;

  public Namespace( @Nonnull final LinkedHashMap<String, String> nameComponents )
  {
    assert null != nameComponents;
    _nameComponents = Collections.unmodifiableMap( new LinkedHashMap<>( nameComponents ) );
  }

  @Nonnull
  public Map<String, String> getNameComponents()
  {
    return _nameComponents;
  }

  @Nonnull
  @Override
  public String toString()
  {
    if( null == _materialized )
    {
      final StringBuilder sb = new StringBuilder();
      boolean addComma = false;
      for( final Map.Entry<String, String> entry : getNameComponents().entrySet() )
      {
        if( addComma )
        {
          sb.append( ',' );
        }
        addComma = true;
        sb.append( entry.getKey() );
        sb.append( '=' );
        sb.append( entry.getValue() );
      }
      _materialized = sb.toString();
    }
    return _materialized;
  }
}
