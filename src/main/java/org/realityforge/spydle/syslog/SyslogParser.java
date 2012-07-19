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
/*
SYSLOG-MSG      = HEADER SP STRUCTURED-DATA [SP MSG]

HEADER          = PRI VERSION SP TIMESTAMP SP HOSTNAME
                  SP APP-NAME SP PROCID SP MSGID
PRI             = "<" PRIVAL ">"
PRIVAL          = 1*3DIGIT ; range 0 .. 191
VERSION         = NONZERO-DIGIT 0*2DIGIT
HOSTNAME        = NILVALUE / 1*255PRINTUSASCII

APP-NAME        = NILVALUE / 1*48PRINTUSASCII
PROCID          = NILVALUE / 1*128PRINTUSASCII
MSGID           = NILVALUE / 1*32PRINTUSASCII

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


STRUCTURED-DATA = NILVALUE / 1*SD-ELEMENT
SD-ELEMENT      = "[" SD-ID *(SP SD-PARAM) "]"
SD-PARAM        = PARAM-NAME "=" %d34 PARAM-VALUE %d34
SD-ID           = SD-NAME
PARAM-NAME      = SD-NAME
PARAM-VALUE     = UTF-8-STRING ; characters '"', '\' and
                               ; ']' MUST be escaped.
SD-NAME         = 1*32PRINTUSASCII
                  ; except '=', SP, ']', %d34 (")

MSG             = MSG-ANY / MSG-UTF8
MSG-ANY         = *OCTET ; not starting with BOM
MSG-UTF8        = BOM UTF-8-STRING
BOM             = %xEF.BB.BF

UTF-8-STRING    = *OCTET ; UTF-8 string as specified
                  ; in RFC 3629

OCTET           = %d00-255
SP              = %d32
PRINTUSASCII    = %d33-126
NONZERO-DIGIT   = %d49-57
DIGIT           = %d48 / NONZERO-DIGIT
NILVALUE        = "-"
  */
    final int facility;
    final int level;
    final Date timestamp;
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
      throw new IllegalArgumentException( "Unknown content trailing Hostname: " + message );
    }

    final int startAppName = endHost + 1;
    final int endAppName = message.indexOf( SP, startAppName );
    if( -1 == endAppName )
    {
      throw new IllegalArgumentException( "Message truncated after AppName: " + message );
    }
    final String appNameString = message.substring( startAppName, endAppName );
    final String appName = "-".equals( appNameString ) ? null : appNameString;

    final int startProcId = endAppName + 1;
    final int endProcId = message.indexOf( SP, startProcId );
    if( -1 == endProcId )
    {
      throw new IllegalArgumentException( "Message truncated after ProcId: " + message );
    }
    final String procIdString = message.substring( startProcId, endProcId );
    final String procId = "-".equals( procIdString ) ? null : procIdString;

    final int startMsgId = endProcId + 1;
    final int endMsgId = message.indexOf( SP, startMsgId );
    if( -1 == endMsgId )
    {
      throw new IllegalArgumentException( "Message truncated after MsgId: " + message );
    }
    final String msgIdString = message.substring( startMsgId, endMsgId );
    final String msgId = "-".equals( msgIdString ) ? null : msgIdString;

    return new SyslogMessage( facility, level, timestamp, hostname, appName, procId, msgId );
  }
}
