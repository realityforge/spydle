package org.realityforge.spydle.syslog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.joda.time.DateTime;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class SyslogMessageTest
{
  @DataProvider( name = "validRFC3164Messages" )
  public Object[][] validRFC3164Messages()
  {
    return new Object[][]
      {
        { "<34>Oct 11 22:14:15 mymachine su[21]: 'su root' failed for lonvick on /dev/pts/8", true },
        //No ProcID
        { "<34>Oct 11 22:14:15 mymachine su: 'su root' failed for lonvick on /dev/pts/8", true },
        //No tag
        { "<34>Oct 11 22:14:15 mymachine 'su root' failed for lonvick on /dev/pts/8", true },
        //No PRI
        { "Use the BFG!", false },
        //No host
        { "<13>Feb  5 17:32:18 10.0.0.99 Use the BFG!", false },
        //Date in incorrect format so mashed
        { "<165>Aug 24 05:34:00 CST 1987 mymachine myproc[10]: %% It's time to make the do-nuts.  %%  Ingredients: Mix=OK, Jelly=OK # Devices: Mixer=OK, Jelly_Injector=OK, Frier=OK # Transport: Conveyer1=OK, Conveyer2=OK # %%", false },
        //Date in incorrect format so mashed
        { "<0>1990 Oct 22 10:52:01 TZ-6 scapegoat.dmz.example.org 10.1.2.3 sched[0]: That's All Folks!", false },
      };
  }

  @DataProvider( name = "ValidStructuredMessages" )
  public Object[][] validStructuredMessages()
  {
    return new Object[][]
      {
        { "<34>1 2003-10-11T22:14:15.003Z mymachine.example.com su - ID47 - BOM'su root' failed for lonvick on /dev/pts/8", true },
        { "<34>1 1985-04-12T19:20:50.52-04:00 mymachine.example.com su - ID47 - BOM'su root' failed for lonvick on /dev/pts/8", false },
        { "<34>1 1985-04-12T23:20:50.52Z mymachine.example.com su - ID47 - BOM'su root' failed for lonvick on /dev/pts/8", false },
        { "<34>1 2003-10-11T22:14:15.003Z mymachine.example.com su - ID47 - BOM'su root' failed for lonvick on /dev/pts/8", true },
        { "<34>1 2003-08-24T05:14:15.000003-07:00 mymachine.example.com su - ID47 - BOM'su root' failed for lonvick on /dev/pts/8", false },
        { "<34>1 - mymachine.example.com su - ID47 - BOM'su root' failed for lonvick on /dev/pts/8", true },
        { "<165>1 2003-08-24T05:14:15.000003-07:00 192.0.2.1 myproc 8710 - - %% It's time to make the do-nuts.", false },
        { "<165>1 2003-10-11T22:14:15.003Z mymachine.example.com evntslog - ID47 [exampleSDID@32473 iut=\"3\" eventSource=\"Application\" eventID=\"1011\"][examplePriority@32473 class=\"high\"]", true },
        { "<165>1 2003-10-11T22:14:15.003Z mymachine.example.com - - - [exampleSDID@32473 iut=\"\\\\3\" eventSource=\"\\\"Application\\\"\" eventID=\"10\\]11\"][examplePriority@32473 class=\"high\"]", true },
        { "<165>1 2003-10-11T22:14:15.003Z mymachine.example.com evntslog - ID47 [exampleSDID@32473 iut=\"3\" eventSource=\"Application\" eventID=\"1011\"][examplePriority@32473 class=\"high\"] - and thats a wrap!", true },
        { "<165>1 - - - - - -", true }
      };
  }

  @DataProvider( name = "InvalidStructuredMessages" )
  public Object[][] invalidStructuredMessages()
  {
    return new Object[][]
      {
        { "XXXX", "Missing < to start PRI: " },
        { "<1 XXXX", "Missing > to finish PRI: " },
        { "<> XXXX", "Failed to parse PRI: " },
        { "<34>1", "Missing SP to terminate version: " },
        { "<34>2 ", "Unknown version: " },
        { "<165>1 2003-10-11T22:14:15.003Z", "Message truncated after timestamp: " },
        { "<165>1 2003-10-11T22:14:15.003Z myhost", "Message truncated after hostname: " },
        { "<165>1 2003-10-11T22:14:15.003Z myhost myapp", "Message truncated after AppName: " },
        { "<165>1 2003-10-11T22:14:15.003Z myhost myapp 22", "Message truncated after ProcId: " },
        { "<165>1 2003-10-11T22:14:15.003Z myhost myapp 22 Msg1", "Message truncated after MsgId: " },
        { "<165>1 2003-10-11T22:14:15.003Z myhost myapp 22 Msg1", "Message truncated after MsgId: " },
        { "<165>1 2003-10-11T22:14:15.003Z myhost myapp 22 Msg1 [X X=\"\"x]", "Missing space at start of param: " },
        { "<165>1 2003-10-11T22:14:15.003Z myhost myapp 22 Msg1 [X X \"\"]", "Param name not followed by =: " },
        { "<165>1 2003-10-11T22:14:15.003Z myhost myapp 22 Msg1 [X X=x\"]", "Param value not started by \": " },
        { "<165>1 2003-10-11T22:14:15.003Z myhost myapp 22 Msg1 [X X=\"x]", "Message terminated unexpectedly: " },
        { "<165>1 2003-10-11T22:14:15.003Z myhost myapp 22 Msg1 [X X=\"x\" helo", "Message terminated unexpectedly: " },
        { "<165>1 2003-10-11T22:14:15.003Z myhost myapp 22 Msg1 -X", "Missing SP after structured data: " },
      };
  }

  @Test( dataProvider = "InvalidStructuredMessages" )
  public void failInvalidStructuredSyslogMessages( final String rawMessage1, final String messagePrefix )
  {
    try
    {
      SyslogMessage.parseStructuredSyslogMessage( rawMessage1 );
    }
    catch( final Exception e )
    {
      final String message = e.getMessage();
      assertTrue( message.startsWith( messagePrefix ), message + " does not start with " + messagePrefix );
      assertTrue( message.endsWith( rawMessage1 ), message + " does not end with " + rawMessage1 );
      return;
    }
    fail( "Unexpectedly parsed " + rawMessage1 + " but expected failure with " + messagePrefix );
  }

  @Test( dataProvider = "validRFC3164Messages" )
  public void parseValidRFC3164Messages( final String rawMessage1, final boolean expectRawMessageIsBytePerfect )
  {
    final SyslogMessage syslogMessage1 = SyslogMessage.parseRFC3164SyslogMessage( rawMessage1 );
    final String rawMessage2 = syslogMessage1.asStructuredSyslogMessage();
    if( expectRawMessageIsBytePerfect )
    {
      //assertEquals( rawMessage1, rawMessage2 );
    }
    //final SyslogMessage syslogMessage2 = SyslogMessage.parseRFC3164SyslogMessage( rawMessage2 );
    //assertEquals( syslogMessage1, syslogMessage2 );
  }

  @Test( dataProvider = "ValidStructuredMessages" )
  public void parseValidStructuredSyslogMessages( final String rawMessage1, final boolean expectRawMessageIsBytePerfect )
  {
    final SyslogMessage syslogMessage1 = SyslogMessage.parseStructuredSyslogMessage( rawMessage1 );
    final String rawMessage2 = syslogMessage1.asStructuredSyslogMessage();
    if( expectRawMessageIsBytePerfect )
    {
      assertEquals( rawMessage1, rawMessage2 );
    }
    final SyslogMessage syslogMessage2 = SyslogMessage.parseStructuredSyslogMessage( rawMessage2 );
    assertEquals( syslogMessage1, syslogMessage2 );
  }

  @Test
  public void parseStructuredSyslogMessage()
  {
    final SyslogMessage message =
      SyslogMessage.parseStructuredSyslogMessage( "<34>1 2003-10-11T22:14:15.003Z mymachine.example.com su - ID47 [exampleSDID@32473 iut=\"3\" eventSource=\"Application\" eventID=\"1011\"][examplePriority@32473 class=\"high\"] BOM'su root' failed for lonvick on /dev/pts/8" );
    assertEquals( message.getFacility(), 4 );
    assertEquals( message.getLevel(), 2 );
    final DateTime timestamp = message.getTimestamp();
    assertNotNull( timestamp );
    assertEquals( timestamp.toDate().getTime(), 1065910455003L );
    assertEquals( message.getHostname(), "mymachine.example.com" );
    assertEquals( message.getAppName(), "su" );
    assertNull( message.getProcId() );
    assertEquals( message.getMsgId(), "ID47" );
    final Map<String, List<StructuredDataParameter>> structuredData = message.getStructuredData();
    assertNotNull( structuredData );
    final List<StructuredDataParameter> p1 = structuredData.get( "exampleSDID@32473" );
    assertNotNull( p1 );
    assertEquals( p1.size(), 3 );
    assertEquals( p1.get( 0 ).getName(), "iut" );
    assertEquals( p1.get( 0 ).getValue(), "3" );
    assertEquals( p1.get( 1 ).getName(), "eventSource" );
    assertEquals( p1.get( 1 ).getValue(), "Application" );
    assertEquals( p1.get( 2 ).getName(), "eventID" );
    assertEquals( p1.get( 2 ).getValue(), "1011" );
    final List<StructuredDataParameter> p2 = structuredData.get( "examplePriority@32473" );
    assertNotNull( p2 );
    assertEquals( p2.size(), 1 );
    assertEquals( p2.get( 0 ).getName(), "class" );
    assertEquals( p2.get( 0 ).getValue(), "high" );
    assertEquals( message.getMessage(), "BOM'su root' failed for lonvick on /dev/pts/8" );
  }

  @Test
  public void parseRFC3164SyslogMessage()
  {
    final SyslogMessage message =
      SyslogMessage.parseRFC3164SyslogMessage( "<34>Oct 11 22:14:15 mymachine su[21]: 'su root' failed for lonvick on /dev/pts/8" );
    assertEquals( message.getFacility(), 4 );
    assertEquals( message.getLevel(), 2 );
    final DateTime timestamp = message.getTimestamp();
    assertNotNull( timestamp );
    assertEquals( timestamp.getSecondOfMinute(), 15 );
    assertEquals( timestamp.getMinuteOfHour(), 14 );
    assertEquals( timestamp.getHourOfDay(), 22 );
    assertEquals( timestamp.getDayOfMonth(), 11 );
    assertEquals( timestamp.getMonthOfYear(), 10 );
    assertEquals( message.getHostname(), "mymachine" );
    assertEquals( message.getAppName(), "su" );
    assertEquals( message.getProcId(), "21" );
    assertNull( message.getStructuredData() );
    assertEquals( message.getMessage(), "'su root' failed for lonvick on /dev/pts/8" );
  }

  @Test
  public void parseRFC3164SyslogMessageWithoutTag()
  {
    final SyslogMessage message =
      SyslogMessage.parseRFC3164SyslogMessage( "<34>Oct 11 22:14:15 mymachine 'su root' failed for lonvick on /dev/pts/8" );
    assertEquals( message.getFacility(), 4 );
    assertEquals( message.getLevel(), 2 );
    final DateTime timestamp = message.getTimestamp();
    assertNotNull( timestamp );
    assertEquals( timestamp.getSecondOfMinute(), 15 );
    assertEquals( timestamp.getMinuteOfHour(), 14 );
    assertEquals( timestamp.getHourOfDay(), 22 );
    assertEquals( timestamp.getDayOfMonth(), 11 );
    assertEquals( timestamp.getMonthOfYear(), 10 );
    assertEquals( message.getHostname(), "mymachine" );
    assertNull( message.getAppName() );
    assertNull( message.getProcId() );
    assertNull( message.getStructuredData() );
    assertEquals( message.getMessage(), "'su root' failed for lonvick on /dev/pts/8" );
  }

  @Test
  public void parseRFC3164SyslogMessageWithoutHost()
  {
    final SyslogMessage message =
      SyslogMessage.parseRFC3164SyslogMessage( "<34>Oct 11 22:14:15 Bing!" );
    assertEquals( message.getFacility(), 4 );
    assertEquals( message.getLevel(), 2 );
    final DateTime timestamp = message.getTimestamp();
    assertNotNull( timestamp );
    assertEquals( timestamp.getSecondOfMinute(), 15 );
    assertEquals( timestamp.getMinuteOfHour(), 14 );
    assertEquals( timestamp.getHourOfDay(), 22 );
    assertEquals( timestamp.getDayOfMonth(), 11 );
    assertEquals( timestamp.getMonthOfYear(), 10 );
    assertNull( message.getHostname() );
    assertNull( message.getHostname() );
    assertNull( message.getAppName() );
    assertNull( message.getProcId() );
    assertNull( message.getStructuredData() );
    assertEquals( message.getMessage(), "Bing!" );
  }

  @Test
  public void parseRFC3164SyslogMessageWithoutDate()
  {
    final SyslogMessage message =
      SyslogMessage.parseRFC3164SyslogMessage( "<34>Bing!" );
    assertEquals( message.getFacility(), 4 );
    assertEquals( message.getLevel(), 2 );
    assertNull( message.getTimestamp() );
    assertNull( message.getHostname() );
    assertNull( message.getHostname() );
    assertNull( message.getAppName() );
    assertNull( message.getProcId() );
    assertNull( message.getStructuredData() );
    assertEquals( message.getMessage(), "Bing!" );
  }

  @Test
  public void parseRFC3164SyslogMessageWithoutPRI()
  {
    final SyslogMessage message =
      SyslogMessage.parseRFC3164SyslogMessage( "Bing!" );
    assertEquals( message.getFacility(), -1 );
    assertEquals( message.getLevel(), -1 );
    assertNull( message.getTimestamp() );
    assertNull( message.getHostname() );
    assertNull( message.getHostname() );
    assertNull( message.getAppName() );
    assertNull( message.getProcId() );
    assertNull( message.getStructuredData() );
    assertEquals( message.getMessage(), "Bing!" );
  }

  @Test
  public void equals()
  {
    final int facility = 1;
    final int level = 2;
    final DateTime time = new DateTime();
    final String hostname = "myhost";
    final String appName = "myapp";
    final String procId = "myproc";
    final String msgId = "mymsg";
    final Map<String, List<StructuredDataParameter>> sd = new HashMap<>();
    final ArrayList<StructuredDataParameter> params = new ArrayList<>();
    params.add( new StructuredDataParameter( "key", "myvalue" ) );
    sd.put( "key", params );
    final String text = "message";
    final SyslogMessage message = new SyslogMessage( facility, level, time, hostname, appName, procId, msgId, sd, text );
    assertEquals( message, message );

    final HashMap<String, List<StructuredDataParameter>> structuredData = new HashMap<>( sd );
    final SyslogMessage message2 = new SyslogMessage( facility, level, time, hostname, appName, procId, msgId, structuredData, text );
    assertEquals( message, message2 );
    assertEquals( message.hashCode(), message2.hashCode() );

    final HashMap<String, List<StructuredDataParameter>> structuredData3 = new HashMap<>();
    final ArrayList<StructuredDataParameter> params3 = new ArrayList<>( params );
    structuredData3.put( "key", params3 );
    final SyslogMessage message3 = new SyslogMessage( facility, level, time, hostname, appName, procId, msgId, structuredData3, text );
    assertEquals( message, message3 );
    assertEquals( message.hashCode(), message3.hashCode() );

    assertNotEquals( message, "X" );
    assertNotEquals( message, new SyslogMessage( 44, level, time, hostname, appName, procId, msgId, sd, text ) );
    assertNotEquals( message, new SyslogMessage( facility, 44, time, hostname, appName, procId, msgId, sd, text ) );
    assertNotEquals( message, new SyslogMessage( facility, level, null, hostname, appName, procId, msgId, sd, text ) );
    assertNotEquals( message, new SyslogMessage( facility, level, new DateTime(), hostname, appName, procId, msgId, sd, text ) );
    assertNotEquals( message, new SyslogMessage( facility, level, time, null, appName, procId, msgId, sd, text ) );
    assertNotEquals( message, new SyslogMessage( facility, level, time, "other", appName, procId, msgId, sd, text ) );
    assertNotEquals( message, new SyslogMessage( facility, level, time, hostname, null, procId, msgId, sd, text ) );
    assertNotEquals( message, new SyslogMessage( facility, level, time, hostname, "other", procId, msgId, sd, text ) );
    assertNotEquals( message, new SyslogMessage( facility, level, time, hostname, appName, null, msgId, sd, text ) );
    assertNotEquals( message, new SyslogMessage( facility, level, time, hostname, appName, "other", msgId, sd, text ) );
    assertNotEquals( message, new SyslogMessage( facility, level, time, hostname, appName, procId, null, sd, text ) );
    assertNotEquals( message, new SyslogMessage( facility, level, time, hostname, appName, procId, "other", sd, text ) );
    assertNotEquals( message, new SyslogMessage( facility, level, time, hostname, appName, procId, msgId, null, text ) );

    final Map<String, List<StructuredDataParameter>> sd2 = new HashMap<>();
    final ArrayList<StructuredDataParameter> params2 = new ArrayList<>();
    params2.add( new StructuredDataParameter( "key", "other" ) );
    sd2.put( "key", params2 );
    assertNotEquals( message, new SyslogMessage( facility, level, time, hostname, appName, procId, msgId, sd2, text ) );

    final Map<String, List<StructuredDataParameter>> sd3 = new HashMap<>();
    final ArrayList<StructuredDataParameter> params3b = new ArrayList<>();
    params3b.add( new StructuredDataParameter( "other", "myvalue" ) );
    sd3.put( "key", params3b );
    assertNotEquals( message, new SyslogMessage( facility, level, time, hostname, appName, procId, msgId, sd3, text ) );

    final Map<String, List<StructuredDataParameter>> sd4 = new HashMap<>();
    final ArrayList<StructuredDataParameter> params4 = new ArrayList<>();
    params4.add( new StructuredDataParameter( "key", "myvalue" ) );
    sd4.put( "other", params4 );
    assertNotEquals( message, new SyslogMessage( facility, level, time, hostname, appName, procId, msgId, sd4, text ) );

    assertNotEquals( message, new SyslogMessage( facility, level, time, hostname, appName, procId, msgId, sd, null ) );
    assertNotEquals( message, new SyslogMessage( facility, level, time, hostname, appName, procId, msgId, sd, "other" ) );
  }

  protected static void assertNotEquals( final Object o1, final Object o2 )
  {
    assertFalse( o1.equals( o2 ) );
    assertFalse( o1.hashCode() == o2.hashCode() );
  }
}
