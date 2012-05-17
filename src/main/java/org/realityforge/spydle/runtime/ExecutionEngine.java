package org.realityforge.spydle.runtime;

import java.io.Closeable;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;

/**
 * Class responsible for performing actions in one or more thread pools.
 * This isolates slow running actions from other actions unless they are
 * in the same stage.
 */
public final class ExecutionEngine
  implements Closeable
{
  private final HashMap<String, ThreadPoolExecutor> _executors = new HashMap<>();

  public void execute( @Nonnull final String stage, @Nonnull final Runnable runnable )
  {
    getExecutorForStage( stage ).execute( runnable );
  }

  @Override
  public void close()
  {
    for( final ThreadPoolExecutor executor : _executors.values() )
    {
      executor.shutdownNow();
    }
  }

  private ThreadPoolExecutor getExecutorForStage( @Nonnull final String stage )
  {
    ThreadPoolExecutor executor = _executors.get( stage );
    if( null == executor )
    {
      executor = newExecutor();
      _executors.put( stage, executor );
    }
    return executor;
  }

  private ThreadPoolExecutor newExecutor()
  {
    return new ThreadPoolExecutor( 0,
                                   4,
                                   0L,
                                   TimeUnit.MILLISECONDS,
                                   new LinkedBlockingQueue<Runnable>() );
  }
}
