package org.realityforge.spydle;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.management.ObjectName;

/**
 * A description of a query to ru against a JMX service.
 */
public class QueryDescriptor
{
  private final ObjectName _objectName;
  private final String _metricPrefix;
  private final Set<String> _attributeNames;

  public QueryDescriptor( @Nonnull final ObjectName objectName,
                          @Nullable final String metricPrefix,
                          @Nullable final Set<String> attributeNames )
  {
    _objectName = objectName;

    _metricPrefix = null != metricPrefix ? metricPrefix : cleanObjectName( objectName );

    final HashSet<String> strings = new HashSet<String>();
    if( null != attributeNames )
    {
      strings.addAll( attributeNames );
    }
    _attributeNames = Collections.unmodifiableSet( strings );
  }

  @Nonnull
  public ObjectName getObjectName()
  {
    return _objectName;
  }

  @Nonnull
  public String getMetricPrefix()
  {
    return _metricPrefix;
  }

  @Nonnull
  public Set<String> getAttributeNames()
  {
    return _attributeNames;
  }

  private static String cleanObjectName( final ObjectName objectName )
  {
    return objectName.
      getCanonicalName().
      replace( '.','_' ).
      replace( '=','_' ).
      replace( ':','_' );
  }
}
