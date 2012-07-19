package org.realityforge.spydle.syslog;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import org.joda.time.format.ISODateTimeFormat;

public class SyslogMessage
{
  private static final char NILVALUE = '-';
  private static final char SP = ' ';

  private final int _facility;
  private final int _level;
  @Nullable
  private final Date _timestamp;
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
                        @Nullable final Date timestamp,
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
  public Date getTimestamp()
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

  public static SyslogMessage parseSyslogMessage( final String rawMessage )
  {
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

  private static Date parseDateTime( final String dateString )
  {
    return ISODateTimeFormat.dateTime().parseDateTime( dateString ).toDate();
  }

  private static boolean isNameCharacter( final char ch )
  {
    return SP != ch && '=' != ch & '"' != ch;
  }
}
