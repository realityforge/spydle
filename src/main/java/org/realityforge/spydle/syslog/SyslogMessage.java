package org.realityforge.spydle.syslog;

import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

public class SyslogMessage
{
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
}
