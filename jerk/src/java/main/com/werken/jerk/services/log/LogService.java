package com.werken.jerk.services.log;

import com.werken.jerk.Jerk;
import com.werken.jerk.Channel;
import com.werken.jerk.Service;
import com.werken.jerk.ChannelService;
import com.werken.jerk.Command;
import com.werken.jerk.JerkException;

import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Calendar;
import java.util.Date;

public class LogService implements Service
{
    private File logDir;

    private Properties chanServiceProps;

    private Map channelServices;

    private Timer timer;

    public LogService()
    {
        this.channelServices = new HashMap();
    }

    public void initialize(Jerk jerk,
                           Properties props,
                           Properties chanServiceProps) throws JerkException
    {
        System.err.println( "initializing log services" );

        System.err.println( props );

        String logDirName = props.getProperty( "dir" );

        if ( logDirName == null
             ||
             logDirName.equals( "" ) )
        {
            throw new JerkException( "no dir defined" );
        }

        File logDir = new File( logDirName );

        if ( logDir.exists() )
        {
            if ( ! logDir.isDirectory() )
            {
                throw new JerkException( "dir points to a file, not a directory" );
            }

            if ( ! logDir.canWrite() )
            {
                throw new JerkException( "dir not writable" );
            }

            if ( ! logDir.canRead() )
            {
                throw new JerkException( "dir not readable" );
            }
        }
        else
        {
            if ( logDir.mkdirs() )
            {
                System.err.println( "created log directory " + logDir );
            }
            else
            {
                throw new JerkException( "unable to create log directory " + logDir );
            }
        }

        this.logDir = logDir;
        this.chanServiceProps = chanServiceProps;

        registerRoller();
    }

    protected void registerRoller()
    {
        System.err.println( "registering daily log roller event" );

        Calendar calendar = Calendar.getInstance();

        calendar.setTime( new Date() );

        calendar.set( Calendar.HOUR_OF_DAY,
                      0 );

        calendar.set( Calendar.MINUTE,
                      2 );

        calendar.set( Calendar.SECOND,
                      0 );

        calendar.set( Calendar.MILLISECOND,
                      0 );


        calendar.roll( Calendar.DATE,
                       1 );

        System.err.println( "first roll: " + calendar.getTime() );

        this.timer = new Timer( true );

        this.timer.schedule(
            new TimerTask() 
            {
                public void run()
                {
                    rollAllLogs();
                }
            },
            calendar.getTime(),
            60 * 60 * 24 * 1000
            );
    }

    protected void rollAllLogs()
    {
        System.err.println( "rolling all logs" );

        Iterator serviceIter = this.channelServices.values().iterator();
        LogChannelService eachService = null;

        while ( serviceIter.hasNext() )
        {
            eachService = (LogChannelService) serviceIter.next();

            eachService.rollLogs();
        }
    }

    public File getLogDir()
    {
        return this.logDir;
    }

    public void shutdown()
    {
        System.err.println( "shutting down log services" );
    }

    public ChannelService startChannelService(Channel channel) throws JerkException
    {
        LogChannelService service = new LogChannelService( channel,
                                                           new Properties( this.chanServiceProps ),
                                                           getLogDir() );

        service.initialize();

        this.channelServices.put( channel,
                                  service );

        return service;
    }

    public void stopChannelService(Channel channel) throws JerkException
    {
        LogChannelService service = (LogChannelService) this.channelServices.get( channel );

        service.shutdown();

        this.channelServices.remove( service );
    }

    public Command getCommand()
    {
        return new LogCommand( this );
    }
}
