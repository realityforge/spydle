package org.realityforge.spydle.syslog;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.joda.time.format.ISODateTimeFormat;

public final class SyslogParser
{
  private static final char NILVALUE = '-';
  public static final char SP = ' ';

  private SyslogParser()
  {
  }

  public static SyslogMessage parseSyslogMessage( final String rawMessage )
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
    try
    {
      final int facility;
      final int level;
      final Date timestamp;
      if( '<' != rawMessage.charAt( 0 ) )
      {
        throw new IllegalArgumentException( "Missing < to start PRI: " + rawMessage );
      }
      final int endPri = rawMessage.indexOf( ">" );
      if( endPri < 1 || endPri >= 5 )
      {
        throw new IllegalArgumentException( "Missing > to finish PRI: " + rawMessage );
      }

      final int priority;
      try
      {
        priority = Integer.parseInt( rawMessage.substring( 1, endPri ) );
      }
      catch( final NumberFormatException nfe )
      {
        // Failed to parse PRI
        throw new IllegalArgumentException( "Failed to parse PRI: " + rawMessage );
      }
      facility = priority >> 3;
      level = priority - ( facility << 3 );

      final int startVersion = endPri + 1;
      final int endVersion = rawMessage.indexOf( SP, startVersion );
      if( -1 == endVersion )
      {
        throw new IllegalArgumentException( "Missing SP to terminate version: " + rawMessage );
      }
      if( !"1".equals( rawMessage.substring( startVersion, endVersion ) ) )
      {
        throw new IllegalArgumentException( "Unknown version: " + rawMessage );
      }

      final int startTimestamp = endVersion + 1;
      final int endTimestamp;
      if( NILVALUE == rawMessage.charAt( startTimestamp ) )
      {
        timestamp = null;
        endTimestamp = startTimestamp + 1;
      }
      else
      {
        endTimestamp = rawMessage.indexOf( SP, startTimestamp );
        timestamp = parseDateTime( rawMessage, startTimestamp, endTimestamp );
      }
      if( SP != rawMessage.charAt( endTimestamp ) )
      {
        throw new IllegalArgumentException( "Unknown content trailing timestamp: " + rawMessage );
      }

      final int startHost = endTimestamp + 1;
      final int endHost = rawMessage.indexOf( SP, startHost );
      if( -1 == endHost )
      {
        throw new IllegalArgumentException( "Message truncated after host: " + rawMessage );
      }
      final String hostnameString = rawMessage.substring( startHost, endHost );
      final String hostname = "-".equals( hostnameString ) ? null : hostnameString;

      final int startAppName = endHost + 1;
      final int endAppName = rawMessage.indexOf( SP, startAppName );
      if( -1 == endAppName )
      {
        throw new IllegalArgumentException( "Message truncated after AppName: " + rawMessage );
      }
      final String appNameString = rawMessage.substring( startAppName, endAppName );
      final String appName = "-".equals( appNameString ) ? null : appNameString;

      final int startProcId = endAppName + 1;
      final int endProcId = rawMessage.indexOf( SP, startProcId );
      if( -1 == endProcId )
      {
        throw new IllegalArgumentException( "Message truncated after ProcId: " + rawMessage );
      }
      final String procIdString = rawMessage.substring( startProcId, endProcId );
      final String procId = "-".equals( procIdString ) ? null : procIdString;

      final int startMsgId = endProcId + 1;
      final int endMsgId = rawMessage.indexOf( SP, startMsgId );
      if( -1 == endMsgId )
      {
        throw new IllegalArgumentException( "Message truncated after MsgId: " + rawMessage );
      }
      final String msgIdString = rawMessage.substring( startMsgId, endMsgId );
      final String msgId = "-".equals( msgIdString ) ? null : msgIdString;

      final int startStructuredData = endMsgId + 1;
      final int endStructuredData;
      final Map<String, List<StructuredDataParameter>> structuredData;
      if( NILVALUE == rawMessage.charAt( startStructuredData ) )
      {
        structuredData = null;
        endStructuredData = startStructuredData + 1;
      }
      else
      {
        structuredData = new HashMap<>();
        int index = startStructuredData;
        while( '[' == rawMessage.charAt( index ) )
        {
          index += 1;
          final StringBuilder sb = new StringBuilder();
          char ch;
          while( isNameCharacter( ch = rawMessage.charAt( index ) ) )
          {
            sb.append( ch );
            index++;
          }
          final String sdId = sb.toString();
          sb.setLength( 0 );
          final ArrayList<StructuredDataParameter> params = new ArrayList<>();
          structuredData.put( sdId, params );
          ch = rawMessage.charAt( index );
          while( ']' != ch )
          {
            if( SP != ch )
            {
              throw new IllegalArgumentException( "Missing space at start of param: " + rawMessage );
            }
            index++;
            while( isNameCharacter( ch = rawMessage.charAt( index ) ) )
            {
              sb.append( ch );
              index++;
            }
            final String key = sb.toString();
            sb.setLength( 0 );
            if( '=' != rawMessage.charAt( index ) )
            {
              throw new IllegalArgumentException( "Param name not followed by =: " + rawMessage );
            }
            index++;
            if( '"' != rawMessage.charAt( index ) )
            {
              throw new IllegalArgumentException( "Param value not started by \": " + rawMessage );
            }
            index++;

            while( '"' != ( ch = rawMessage.charAt( index ) ) )
            {
              index++;
              if( '\\' == ch )
              {
                index++;
                sb.append( rawMessage.charAt( index ) );
              }
              else
              {
                sb.append( ch );
              }
            }
            index++;
            final String value = sb.toString();
            sb.setLength( 0 );
            params.add( new StructuredDataParameter( key, value ) );
            ch = rawMessage.charAt( index );
          }
          if( ']' != rawMessage.charAt( index ) )
          {
            throw new IllegalArgumentException( "Missing ] at end of structured data: " + rawMessage );
          }
          index++;
          if( index == rawMessage.length() )
          {
            break;
          }
        }
        endStructuredData = index;
      }

      final String message;
      if( rawMessage.length() != endStructuredData )
      {
        if( SP != rawMessage.charAt( endStructuredData ) )
        {
          throw new IllegalArgumentException( "Missing SP after structured data: " + rawMessage );
        }
        final int startMessage = endStructuredData + 1;
        message = rawMessage.substring( startMessage );
      }
      else
      {
        message = null;
      }

      return new SyslogMessage( facility,
                                level,
                                timestamp,
                                hostname,
                                appName,
                                procId,
                                msgId,
                                structuredData,
                                message );
    }
    catch( final StringIndexOutOfBoundsException obe )
    {
      throw new IllegalArgumentException( "Message terminated unexpectedly: " + rawMessage, obe );
    }
  }

  private static Date parseDateTime( final String rawMessage, final int startTimestamp, final int endTimestamp )
  {
    return ISODateTimeFormat.dateTime().parseDateTime( rawMessage.substring( startTimestamp, endTimestamp ) ).toDate();
  }

  private static boolean isNameCharacter( final char ch )
  {
    return SP != ch && '=' != ch & '"' != ch;
  }
}
