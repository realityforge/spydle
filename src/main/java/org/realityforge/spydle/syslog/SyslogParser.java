package org.realityforge.spydle.syslog;

import java.util.Date;
import org.joda.time.format.ISODateTimeFormat;

public final class SyslogParser
{
  private static final char NILVALUE = '-';
  public static final char SP = ' ';

  private SyslogParser()
  {
  }

  public static SyslogMessage parseSyslogMessage( final String message )
  {
    final int facility;
    final int level;
    final Date timestamp;
/*
PRI             = "<" PRIVAL ">"
PRIVAL          = 1*3DIGIT ; range 0 .. 191
*/
    if( '<' != message.charAt( 0 ) )
    {
      throw new IllegalArgumentException( "Missing < to start PRI: " + message );
    }
    final int endPri = message.indexOf( ">" );
    if( endPri < 1 || endPri >= 5 )
    {
      throw new IllegalArgumentException( "Missing > to finish PRI: " + message );
    }

    final int priority;
    try
    {
      priority = Integer.parseInt( message.substring( 1, endPri ) );
    }
    catch( final NumberFormatException nfe )
    {
      // Failed to parse PRI
      throw new IllegalArgumentException( "Failed to parse PRI: " + message );
    }
    facility = priority >> 3;
    level = priority - ( facility << 3 );
/*
VERSION         = NONZERO-DIGIT 0*2DIGIT
*/
    final int startVersion = endPri + 1;
    final int endVersion = message.indexOf( SP, startVersion );
    if( -1 == endVersion )
    {
      throw new IllegalArgumentException( "Missing SP to terminate version: " + message );
    }
    if( !"1".equals( message.substring( startVersion, endVersion ) ) )
    {
      throw new IllegalArgumentException( "Unknown version: " + message );
    }
    final int startTimestamp = endVersion + 1;

    /*
TIMESTAMP       = NILVALUE / FULL-DATE "T" FULL-TIME
FULL-DATE       = DATE-FULLYEAR "-" DATE-MONTH "-" DATE-MDAY
DATE-FULLYEAR   = 4DIGIT
DATE-MONTH      = 2DIGIT  ; 01-12
DATE-MDAY       = 2DIGIT  ; 01-28, 01-29, 01-30, 01-31 based on
                          ; month/year
FULL-TIME       = PARTIAL-TIME TIME-OFFSET
PARTIAL-TIME    = TIME-HOUR ":" TIME-MINUTE ":" TIME-SECOND
                  [TIME-SECFRAC]
TIME-HOUR       = 2DIGIT  ; 00-23
TIME-MINUTE     = 2DIGIT  ; 00-59
TIME-SECOND     = 2DIGIT  ; 00-59
TIME-SECFRAC    = "." 1*6DIGIT
TIME-OFFSET     = "Z" / TIME-NUMOFFSET
TIME-NUMOFFSET  = ("+" / "-") TIME-HOUR ":" TIME-MINUTE
     */
    final int endTimestamp;
    if( NILVALUE == message.charAt( startTimestamp ) )
    {
      timestamp = null;
      endTimestamp = startTimestamp + 1;
    }
    else
    {
      endTimestamp = message.indexOf( SP, startTimestamp );
      timestamp = ISODateTimeFormat.dateTime().parseDateTime( message.substring( startTimestamp, endTimestamp ) ).toDate();
    }
    if( SP != message.charAt( endTimestamp ) )
    {
      throw new IllegalArgumentException( "Unknown content trailing timestamp: " + message );
    }

/*
HOSTNAME        = NILVALUE / 1*255PRINTUSASCII
*/
    final int startHost = endTimestamp + 1;
    final int endHost = message.indexOf( SP, startHost );
    if( -1 == endHost )
    {
      throw new IllegalArgumentException( "Message truncated after host: " + message );
    }
    final String hostnameString = message.substring( startHost, endHost );
    final String hostname = "-".equals( hostnameString ) ? null : hostnameString;

    if( SP != message.charAt( endHost ) )
    {
      throw new IllegalArgumentException( "Unknown content trailing hostname: " + message );
    }

    final int startAppName = endHost + 1;
    final int endAppName = message.indexOf( SP, startAppName );
    if( -1 == endAppName )
    {
      throw new IllegalArgumentException( "Message truncated after appname: " + message );
    }
    final String appNameString = message.substring( startAppName, endAppName );
    final String appName = "-".equals( appNameString ) ? null : appNameString;


    return new SyslogMessage( facility, level, timestamp, hostname, appName );
  }
}
