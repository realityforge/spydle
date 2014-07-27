package org.realityforge.spydle.util;

/**
 * Utility class for processing json configuration files.
 */
public final class ConfigUtil
{
  private ConfigUtil()
  {
  }

  public static void appendNameElement( final StringBuilder sb, final String element )
  {
    if( null != element )
    {
      if( 0 != sb.length() )
      {
        sb.append( '.' );
      }
      final int length = element.length();
      for( int i = 0; i < length; i++ )
      {
        final char c = element.charAt( i );
        if( '_' == c || Character.isLetterOrDigit( c ) )
        {
          sb.append( c );
        }
        else
        {
          sb.append( '_' );
        }
      }
    }
  }
}
