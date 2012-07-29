package org.realityforge.spydle.jmx;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.management.ObjectName;
import org.realityforge.spydle.util.ConfigUtil;

/**
 * A description of a query to run against a JMX service.
 */
public class JmxProbeDescriptor
{
  public static final String DOMAIN_COMPONENT = "DOMAIN";
  public static final String ATTRIBUTE_COMPONENT = "ATTRIBUTE";

  private final ObjectName _objectName;
  private final String _namespace;
  private final Set<String> _attributeNames;
  private final List<String> _nameComponents;

  public JmxProbeDescriptor( @Nonnull final ObjectName objectName,
                             @Nullable final Set<String> attributeNames,
                             @Nullable final String namespace,
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
  public String getNamespace()
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
  public String generateKey( @Nonnull final ObjectName objectName,
                             @Nonnull final String attribute )
  {
    final StringBuilder sb = new StringBuilder();
    ConfigUtil.appendNameElement( sb, getNamespace() );
    final List<String> nameComponents = getNameComponents();
    if( null == nameComponents )
    {
      ConfigUtil.appendNameElement( sb, objectName.getDomain() );
      for( final Map.Entry<String, String> entry : objectName.getKeyPropertyList().entrySet() )
      {
        ConfigUtil.appendNameElement( sb, entry.getValue() );
      }
    }
    else
    {
      for( final String nameComponent : nameComponents )
      {
        final String value =
          nameComponent.equals( ATTRIBUTE_COMPONENT ) ?
            attribute :
            nameComponent.equals( DOMAIN_COMPONENT ) ?
              objectName.getDomain() :
              objectName.getKeyProperty( nameComponent );
        ConfigUtil.appendNameElement( sb, value );
      }
    }

    ConfigUtil.appendNameElement( sb, attribute );
    return sb.toString();
  }
}
