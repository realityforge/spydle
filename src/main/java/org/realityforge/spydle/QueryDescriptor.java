package org.realityforge.spydle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.management.ObjectName;

/**
 * A description of a query to ru against a JMX service.
 */
public class QueryDescriptor
{
  public static final String DOMAIN_COMPONENT = "DOMAIN";
  public static final String ATTRIBUTE_COMPONENT = "ATTRIBUTE";

  private final ObjectName _objectName;
  private final String _namePrefix;
  private final Set<String> _attributeNames;
  private final List<String> _nameComponents;

  public QueryDescriptor( @Nonnull final ObjectName objectName,
                          @Nullable final Set<String> attributeNames,
                          @Nullable final String namePrefix )
  {
    this( objectName, attributeNames, namePrefix, null );
  }

  public QueryDescriptor( @Nonnull final ObjectName objectName,
                          @Nullable final Set<String> attributeNames,
                          @Nullable final String namePrefix,
                          @Nullable final List<String> nameComponents )
  {
    _objectName = objectName;
    _attributeNames =
      null != attributeNames ? Collections.unmodifiableSet( new HashSet<String>( attributeNames ) ) : null;
    _namePrefix = namePrefix;
    _nameComponents = null != nameComponents ? Collections.unmodifiableList( new ArrayList<String>( nameComponents ) ) : null;
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
  public String getNamePrefix()
  {
    return _namePrefix;
  }

  @Nullable
  public List<String> getNameComponents()
  {
    return _nameComponents;
  }

  public String generateKey( final ObjectName objectName, final String attribute )
  {
    final String namePrefix = getNamePrefix();
    final List<String> nameComponents = getNameComponents();
    if( null == nameComponents )
    {
      final String prefix = namePrefix == null ? "" : namePrefix + '.';
      return prefix + cleanString( objectName.getCanonicalName() ) + '.' + attribute;
    }
    else
    {
      final StringBuilder sb = new StringBuilder();
      if( null != namePrefix )
      {
        sb.append( namePrefix );
      }
      boolean addedAttributeComponent = false;
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
          appendNameComponent( sb, value );
          addedAttributeComponent = addedAttributeComponent || attributeComponent;
        }
      }
      if( !addedAttributeComponent )
      {
        appendNameComponent( sb, attribute );
      }
      return sb.toString();
    }
  }

  private void appendNameComponent( final StringBuilder sb, final String value )
  {
    if( 0 != sb.length() )
    {
      sb.append( '.' );
    }
    sb.append( cleanString( value ) );
  }

  private static String cleanString( final String name )
  {
    return name.
      replace( '.', '_' ).
      replace( '=', '_' ).
      replace( ':', '_' );
  }
}
