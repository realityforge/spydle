package org.realityforge.spydle.runtime;

import javax.annotation.Nonnull;

/**
 * The representation of the name of a value in the system. The representation is immutable.
 */
public final class MetricName
{
  private final Namespace _namespace;
  private final String _key;
  private String _materialized;

  public MetricName( @Nonnull final Namespace namespace,
                     @Nonnull final String key )
  {
    assert null != namespace;
    assert null != key;
    _namespace = namespace;
    _key = key;
  }

  @Nonnull
  public Namespace getNamespace()
  {
    return _namespace;
  }

  @Nonnull
  public String getKey()
  {
    return _key;
  }

  @Nonnull
  @Override
  public String toString()
  {
    if( null == _materialized )
    {
      final StringBuilder sb = new StringBuilder();
      sb.append( getNamespace().toString() );
      sb.append( ',' );
      sb.append( getKey() );
      _materialized = sb.toString();
    }
    return _materialized;
  }
}
