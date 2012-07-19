package org.realityforge.spydle.syslog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

public class SyslogMessage
{
  private static final String NILVALUE_STRING = "-";
  private static final char NILVALUE = '-';
  private static final char SP = ' ';
  private static final char PRI_START = '<';
  private static final char PRI_END = '>';
  private static final String VERSION = "1";
  private static final char SD_END = ']';
  private static final char SD_START = '[';
  private static final char SD_VALUE_QUOTE = '"';
  private static final char SD_ASSIGN = '=';

  private final int _facility;
  private final int _level;
  @Nullable
  private final DateTime _timestamp;
  @Nullable
  private final String _hostname;
  @Nullable
  private final String _appName;
  @Nullable
  private final String _procId;
  @Nullable
  private final String _msgId;
  @Nullable
  private final Map<String, List<StructuredDataParameter>> _structuredData;
  @Nullable
  private final String _message;

  public SyslogMessage( final int facility,
                        final int level,
                        @Nullable final DateTime timestamp,
                        @Nullable final String hostname,
                        @Nullable final String appName,
                        @Nullable final String procId,
                        @Nullable final String msgId,
                        @Nullable final Map<String, List<StructuredDataParameter>> structuredData,
                        @Nullable final String message )
  {
    _facility = facility;
    _level = level;
    _timestamp = timestamp;
    _hostname = hostname;
    _appName = appName;
    _procId = procId;
    _msgId = msgId;
    _structuredData = structuredData;
    _message = message;
  }

  public int getFacility()
  {
    return _facility;
  }

  public int getLevel()
  {
    return _level;
  }

  @Nullable
  public DateTime getTimestamp()
  {
    return _timestamp;
  }

  @Nullable
  public String getHostname()
  {
    return _hostname;
  }

  @Nullable
  public String getAppName()
  {
    return _appName;
  }

  @Nullable
  public String getProcId()
  {
    return _procId;
  }

  @Nullable
  public String getMsgId()
  {
    return _msgId;
  }

  @Nullable
  public Map<String, List<StructuredDataParameter>> getStructuredData()
  {
    return _structuredData;
  }

  @Nullable
  public String getMessage()
  {
    return _message;
  }

  @Override
  public boolean equals( final Object o )
  {
    if( this == o )
    {
      return true;
    }
    else if( o == null || getClass() != o.getClass() )
    {
      return false;
    }

    final SyslogMessage that = (SyslogMessage) o;
    if( _facility != that._facility )
    {
      return false;
    }
    else if( _level != that._level )
    {
      return false;
    }
    else if( _appName != null ? !_appName.equals( that._appName ) : that._appName != null )
    {
      return false;
    }
    else if( _hostname != null ? !_hostname.equals( that._hostname ) : that._hostname != null )
    {
      return false;
    }
    else if( _message != null ? !_message.equals( that._message ) : that._message != null )
    {
      return false;
    }
    else if( _msgId != null ? !_msgId.equals( that._msgId ) : that._msgId != null )
    {
      return false;
    }
    else if( _procId != null ? !_procId.equals( that._procId ) : that._procId != null )
    {
      return false;
    }
    else if( _structuredData != null ? !_structuredData.equals( that._structuredData ) : that._structuredData != null )
    {
      return false;
    }
    else
    {
      return !( _timestamp != null ? !_timestamp.equals( that._timestamp ) : that._timestamp != null );
    }
  }

  @Override
  public int hashCode()
  {
    int result = _facility;
    result = 31 * result + _level;
    result = 31 * result + ( _timestamp != null ? _timestamp.hashCode() : 0 );
    result = 31 * result + ( _hostname != null ? _hostname.hashCode() : 0 );
    result = 31 * result + ( _appName != null ? _appName.hashCode() : 0 );
    result = 31 * result + ( _procId != null ? _procId.hashCode() : 0 );
    result = 31 * result + ( _msgId != null ? _msgId.hashCode() : 0 );
    result = 31 * result + ( _structuredData != null ? _structuredData.hashCode() : 0 );
    result = 31 * result + ( _message != null ? _message.hashCode() : 0 );
    return result;
  }

  @Override
  public String toString()
  {
    final DateTime ts = getTimestamp();
    final String timestamp = ts == null ? NILVALUE_STRING : emitTimestamp( ts );
    final String hostname = getHostname() == null ? NILVALUE_STRING : getHostname();
    final String appName = getAppName() == null ? NILVALUE_STRING : getAppName();
    final String procId = getProcId() == null ? NILVALUE_STRING : getProcId();
    final String msgId = getMsgId() == null ? NILVALUE_STRING : getMsgId();
    final int pri = getLevel() + ( getFacility() << 3 );
    final String sd;
    final Map<String, List<StructuredDataParameter>> structuredData = getStructuredData();
    if( null != structuredData )
    {
      final StringBuilder sb = new StringBuilder();
      for( final Map.Entry<String, List<StructuredDataParameter>> entry : structuredData.entrySet() )
      {
        sb.append( SD_START );
        sb.append( entry.getKey() );
        for( final StructuredDataParameter param : entry.getValue() )
        {
          sb.append( SP );
          sb.append( param.getName() );
          sb.append( SD_ASSIGN );
          sb.append( SD_VALUE_QUOTE );
          sb.append( param.getValue().replace( "\\", "\\\\" ).replace( "]", "\\]" ).replace( "\"", "\\\"" ) );
          sb.append( SD_VALUE_QUOTE );
        }

        sb.append( SD_END );
      }
      sd = sb.toString();
    }
    else
    {
      sd = NILVALUE_STRING;
    }
    final String messageSuffix = getMessage() == null ? "" : SP + getMessage();
    return String.valueOf( PRI_START ) + pri + String.valueOf( PRI_END ) + VERSION +
           SP + timestamp +
           SP + hostname +
           SP + appName +
           SP + procId +
           SP + msgId +
           SP + sd +
           messageSuffix;
  }

  public static SyslogMessage parseSyslogMessage( final String rawMessage )
  {
    try
    {
      if( PRI_START != rawMessage.charAt( 0 ) )
      {
        throw new IllegalArgumentException( "Missing < to start PRI: " + rawMessage );
      }
      final int endPri = rawMessage.indexOf( PRI_END );
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
      final int facility = priority >> 3;
      final int level = priority - ( facility << 3 );

      final int startVersion = endPri + 1;
      final int endVersion = rawMessage.indexOf( SP, startVersion );
      if( -1 == endVersion )
      {
        throw new IllegalArgumentException( "Missing SP to terminate version: " + rawMessage );
      }
      if( !VERSION.equals( rawMessage.substring( startVersion, endVersion ) ) )
      {
        throw new IllegalArgumentException( "Unknown version: " + rawMessage );
      }

      final int startTimestamp = endVersion + 1;
      final int endTimestamp;
      final DateTime timestamp;
      if( NILVALUE == rawMessage.charAt( startTimestamp ) )
      {
        timestamp = null;
        endTimestamp = startTimestamp + 1;
      }
      else
      {
        endTimestamp = rawMessage.indexOf( SP, startTimestamp );
        timestamp = parseDateTime( rawMessage.substring( startTimestamp, endTimestamp ) );
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
      final String hostname = NILVALUE_STRING.equals( hostnameString ) ? null : hostnameString;

      final int startAppName = endHost + 1;
      final int endAppName = rawMessage.indexOf( SP, startAppName );
      if( -1 == endAppName )
      {
        throw new IllegalArgumentException( "Message truncated after AppName: " + rawMessage );
      }
      final String appNameString = rawMessage.substring( startAppName, endAppName );
      final String appName = NILVALUE_STRING.equals( appNameString ) ? null : appNameString;

      final int startProcId = endAppName + 1;
      final int endProcId = rawMessage.indexOf( SP, startProcId );
      if( -1 == endProcId )
      {
        throw new IllegalArgumentException( "Message truncated after ProcId: " + rawMessage );
      }
      final String procIdString = rawMessage.substring( startProcId, endProcId );
      final String procId = NILVALUE_STRING.equals( procIdString ) ? null : procIdString;

      final int startMsgId = endProcId + 1;
      final int endMsgId = rawMessage.indexOf( SP, startMsgId );
      if( -1 == endMsgId )
      {
        throw new IllegalArgumentException( "Message truncated after MsgId: " + rawMessage );
      }
      final String msgIdString = rawMessage.substring( startMsgId, endMsgId );
      final String msgId = NILVALUE_STRING.equals( msgIdString ) ? null : msgIdString;

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
        while( SD_START == rawMessage.charAt( index ) )
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
          while( SD_END != ch )
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
            if( SD_ASSIGN != rawMessage.charAt( index ) )
            {
              throw new IllegalArgumentException( "Param name not followed by =: " + rawMessage );
            }
            index++;
            if( SD_VALUE_QUOTE != rawMessage.charAt( index ) )
            {
              throw new IllegalArgumentException( "Param value not started by \": " + rawMessage );
            }
            index++;

            while( SD_VALUE_QUOTE != ( ch = rawMessage.charAt( index ) ) )
            {
              index++;
              if( '\\' == ch )
              {
                sb.append( rawMessage.charAt( index ) );
                index++;
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
          if( SD_END != rawMessage.charAt( index ) )
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

  @Nonnull
  private static DateTime parseDateTime( @Nonnull final String dateString )
  {
    return ISODateTimeFormat.dateTime().withOffsetParsed().parseDateTime( dateString );
  }

  @Nonnull
  private static String emitTimestamp( @Nonnull final DateTime date )
  {
    final DateTimeFormatter dateTimeFormatter = ISODateTimeFormat.dateTime();
    dateTimeFormatter.withChronology( date.getChronology() );
    dateTimeFormatter.withZone( date.getZone() );
    return dateTimeFormatter.print( date );
  }

  private static boolean isNameCharacter( final char ch )
  {
    return SP != ch && SD_ASSIGN != ch & SD_VALUE_QUOTE != ch;
  }
}
