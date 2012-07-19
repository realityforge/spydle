package org.realityforge.spydle.syslog;

import java.util.Date;
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

  public SyslogMessage( final int facility,
                        final int level,
                        final Date timestamp,
                        final String hostname,
                        final String appName,
                        final String procId )
  {
    _facility = facility;
    _level = level;
    _timestamp = timestamp;
    _hostname = hostname;
    _appName = appName;
    _procId = procId;
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
}
