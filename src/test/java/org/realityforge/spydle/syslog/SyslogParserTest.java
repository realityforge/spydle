package org.realityforge.spydle.syslog;

import java.util.Date;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class SyslogParserTest

/*
<165>1 2003-10-11T22:14:15.003Z mymachine.example.com evntslog - ID47 [exampleSDID@32473 iut="3" eventSource="Application" eventID="1011"][examplePriority@32473 class="high"]
 */
{
  @DataProvider( name = "ValidMessages" )
  public Object[][] validMessages()
  {
    return new Object[][]
      {
        { "<34>1 2003-10-11T22:14:15.003Z mymachine.example.com su - ID47 - BOM'su root' failed for lonvick on /dev/pts/8" },
        { "<34>1 1985-04-12T19:20:50.52-04:00 mymachine.example.com su - ID47 - BOM'su root' failed for lonvick on /dev/pts/8" },
        { "<34>1 1985-04-12T23:20:50.52Z mymachine.example.com su - ID47 - BOM'su root' failed for lonvick on /dev/pts/8" },
        { "<34>1 2003-10-11T22:14:15.003Z mymachine.example.com su - ID47 - BOM'su root' failed for lonvick on /dev/pts/8" },
        { "<34>1 2003-08-24T05:14:15.000003-07:00 mymachine.example.com su - ID47 - BOM'su root' failed for lonvick on /dev/pts/8" },
        { "<34>1 - mymachine.example.com su - ID47 - BOM'su root' failed for lonvick on /dev/pts/8" },
        { "<165>1 2003-08-24T05:14:15.000003-07:00 192.0.2.1 myproc 8710 - - %% It's time to make the do-nuts." },
        { "<165>1 2003-10-11T22:14:15.003Z mymachine.example.com evntslog - ID47 [exampleSDID@32473 iut=\"3\" eventSource=\"Application\" eventID=\"1011\"][examplePriority@32473 class=\"high\"]" }
      };
  }

  @DataProvider( name = "InvalidMessages" )
  public Object[][] invalidMessages()
  {
    return new Object[][]
      {
        //Invalid timestamp
        { "<34>1 2003-08-24T05:14:15.000000003-07:00 mymachine.example.com su - ID47 - BOM'su root' failed for lonvick on /dev/pts/8" },
      };
  }

  @Test( dataProvider = "ValidMessages" )
  public void parseValidMessages( final String rawMessage )
  {
    SyslogParser.parseSyslogMessage( rawMessage );
  }

  @Test
  public void simpleParse()
  {
    final SyslogMessage message =
      SyslogParser.parseSyslogMessage( "<34>1 2003-10-11T22:14:15.003Z mymachine.example.com su - ID47 - BOM'su root' failed for lonvick on /dev/pts/8" );
    assertEquals( message.getFacility(), 4 );
    assertEquals( message.getLevel(), 2 );
    final Date timestamp = message.getTimestamp();
    assertNotNull( timestamp );
    assertEquals( timestamp.getTime(), 1065910455003L );
    assertEquals( message.getHostname(), "mymachine.example.com" );
    assertEquals( message.getAppName(), "su" );
    assertNull( message.getProcId() );
    assertEquals( message.getMsgId(), "ID47" );
  }
}
