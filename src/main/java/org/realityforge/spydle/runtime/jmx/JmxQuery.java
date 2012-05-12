package org.realityforge.spydle.runtime.jmx;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.management.ObjectName;
import org.realityforge.spydle.runtime.MetricName;
import org.realityforge.spydle.runtime.Namespace;

/**
 * A description of a query to run against a JMX service.
 */
public class JmxQuery
{
  public static final String DOMAIN_COMPONENT = "DOMAIN";
  public static final String ATTRIBUTE_COMPONENT = "ATTRIBUTE";

  private final ObjectName _objectName;
  private final Namespace _namespace;
  private final Set<String> _attributeNames;
  private final List<String> _nameComponents;

  public JmxQuery( @Nonnull final ObjectName objectName,
                   @Nullable final Set<String> attributeNames,
                   @Nullable final Namespace namespace )
  {
    this( objectName, attributeNames, namespace, null );
  }

  public JmxQuery( @Nonnull final ObjectName objectName,
                   @Nullable final Set<String> attributeNames,
                   @Nullable final Namespace namespace,
                   @Nullable final List<String> nameComponents )
  {
    _objectName = objectName;
    _attributeNames =
      null != attributeNames ? Collections.unmodifiableSet( new HashSet<>( attributeNames ) ) : null;
    _namespace = namespace;
    _nameComponents = null != nameComponents ? Collections.unmodifiableList( new ArrayList<>( nameComponents ) ) : null;
  }

  @Nonnull
  public ObjectName getObjectName()
  {
    return _objectName;
  }

  @Nullable
  public Set<String> getAttributeNames()
  {
    return _attributeNames;
  }

  @Nullable
  public Namespace getNamespace()
  {
    return _namespace;
  }

  @Nullable
  public List<String> getNameComponents()
  {
    return _nameComponents;
  }

  @Override
  public String toString()
  {
    return "JmxQuery[objectName=" + _objectName +
           ",attributeNames=" + _attributeNames +
           ",namespace=" + _namespace +
           ",_nameComponents=" + _nameComponents + "]";
  }

  @Nonnull
  public MetricName generateKey( @Nonnull final ObjectName objectName,
                                 @Nonnull final String attribute )
  {
    final LinkedHashMap<String, String> map = new LinkedHashMap<>();
    final Namespace namePrefix = getNamespace();
    if( null != namePrefix )
    {
      map.putAll( namePrefix.getNameComponents() );
    }
    final List<String> nameComponents = getNameComponents();
    if( null == nameComponents )
    {
      map.put( "domain", objectName.getDomain() );
      map.putAll( objectName.getKeyPropertyList() );
    }
    else
    {
      for( final String nameComponent : nameComponents )
      {
        final boolean attributeComponent = nameComponent.equals( ATTRIBUTE_COMPONENT );
        final String value =
          attributeComponent ?
            attribute :
            nameComponent.equals( DOMAIN_COMPONENT ) ?
              objectName.getDomain() :
              objectName.getKeyProperty( nameComponent );
        if( null != value )
        {
          map.put( nameComponent, value );
        }
      }
    }

    return new MetricName( new Namespace( map ), attribute );
  }
}
