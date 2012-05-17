package org.realityforge.spydle;

import java.io.File;
import java.io.IOException;
import java.util.List;
import org.realityforge.cli.CLArgsParser;
import org.realityforge.cli.CLOption;
import org.realityforge.cli.CLOptionDescriptor;
import org.realityforge.cli.CLUtil;
import org.realityforge.spydle.store.MetricRouter;
import org.realityforge.spydle.store.MonitorDataStore;
import org.realityforge.spydle.util.ConfigScanner;

public class Main
{
  private static final String DEFAULT_CONFIG_DIRECTORY = "conf.d";

  private static final int HELP_OPT = 1;
  private static final int VERBOSE_OPT = 'v';
  private static final int CONFIG_DIRECTORY_CONFIG_OPT = 'd';

  private static final CLOptionDescriptor[] OPTIONS = new CLOptionDescriptor[]{
    new CLOptionDescriptor( "help",
                            CLOptionDescriptor.ARGUMENT_DISALLOWED,
                            HELP_OPT,
                            "print this message and exit" ),
    new CLOptionDescriptor( "verbose",
                            CLOptionDescriptor.ARGUMENT_DISALLOWED,
                            VERBOSE_OPT,
                            "print verbose message while sending the message." ),
    new CLOptionDescriptor( "config-directory",
                            CLOptionDescriptor.ARGUMENT_REQUIRED,
                            CONFIG_DIRECTORY_CONFIG_OPT,
                            "the directory in which configuration is read from. Defaults to " + DEFAULT_CONFIG_DIRECTORY ),
  };

  private static final int SUCCESS_EXIT_CODE = 0;
  private static final int ERROR_PARSING_ARGS_EXIT_CODE = 1;

  private static boolean c_verbose;
  private static File c_configDirectory = new File( DEFAULT_CONFIG_DIRECTORY ).getAbsoluteFile();
  private static MonitorDataStore c_dataStore = new MonitorDataStore();

  public static void main( final String[] args )
    throws Exception
  {
    if( !processOptions( args ) )
    {
      System.exit( ERROR_PARSING_ARGS_EXIT_CODE );
      return;
    }

    final ConfigScanner scanner = new ConfigScanner( c_dataStore, c_configDirectory );
    scanner.start();

    final MetricRouter router = new MetricRouter( c_dataStore );

    for( int i = 0; i < 10000000; i++ )
    {
      scanner.scan();
      router.tick();
      Thread.sleep( 100 );
    }

    Runtime.getRuntime().addShutdownHook( new Thread()
    {
      @Override
      public void run()
      {
        try
        {
          c_dataStore.close();
        }
        catch( final IOException ioe )
        {
          //Ignored
        }
        try
        {
          scanner.close();
        }
        catch( final IOException ioe )
        {
          //Ignored
        }

        System.exit( SUCCESS_EXIT_CODE );
      }
    } );
  }

  private static boolean processOptions( final String[] args )
  {
    // Parse the arguments
    final CLArgsParser parser = new CLArgsParser( args, OPTIONS );

    //Make sure that there was no errors parsing arguments
    if( null != parser.getErrorString() )
    {
      error( parser.getErrorString() );
      return false;
    }

    // Get a list of parsed options
    @SuppressWarnings( "unchecked" ) final List<CLOption> options = parser.getArguments();
    for( final CLOption option : options )
    {
      switch( option.getId() )
      {
        case CLOption.TEXT_ARGUMENT:
        {
          error( "Unknown argument specified: " + option.getArgument() );
          return false;
        }
        case CONFIG_DIRECTORY_CONFIG_OPT:
        {
          c_configDirectory = new File( option.getArgument() ).getAbsoluteFile();
          break;
        }
        case VERBOSE_OPT:
        {
          c_verbose = true;
          break;
        }
        case HELP_OPT:
        {
          printUsage();
          return false;
        }
      }
    }

    if( !c_configDirectory.exists() )
    {
      error( "Config directory does not exist: " + c_configDirectory );
      return false;
    }

    if( !c_configDirectory.isDirectory() )
    {
      error( "Config directory is not a directory: " + c_configDirectory );
      return false;
    }

    if( c_verbose )
    {
      info( "Config Directory: " + c_configDirectory );
    }

    return true;
  }

  /**
   * Print out a usage statement
   */
  private static void printUsage()
  {
    final String lineSeparator = System.getProperty( "line.separator" );

    final StringBuilder msg = new StringBuilder();

    msg.append( "java " );
    msg.append( Main.class.getName() );
    msg.append( " [options] message" );
    msg.append( lineSeparator );
    msg.append( "Options: " );
    msg.append( lineSeparator );

    msg.append( CLUtil.describeOptions( OPTIONS ).toString() );

    info( msg.toString() );
  }

  private static void info( final String message )
  {
    System.out.println( message );
  }

  private static void error( final String message )
  {
    System.out.println( "Error: " + message );
  }
}
