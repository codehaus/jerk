package com.werken.jerk.services.log;

import com.werken.jerk.ChannelService;
import com.werken.jerk.Channel;
import com.werken.jerk.Message;
import com.werken.jerk.JerkException;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.Date;
import java.text.DateFormat;

public class LogChannelService implements ChannelService
{
    private Channel channel;
    private Properties props;
    private File logDir;
    private File channelLogDir;

    private Map loggers;

    public LogChannelService(Channel channel,
                             Properties props,
                             File logDir)
    {
        this.channel = channel;
        this.props   = props;
        this.logDir  = logDir;
        this.loggers = new HashMap();
    }

    
    public void rollLogs()
    {
        stopLogs();
        startLogs();
    }

    public void addLogger(String name,
                          Logger logger)
    {
        logger.setChannel( this.channel );
        this.loggers.put( name,
                          logger );
    }

    public void startLogs()
    {
        String baseName = DateFormat.getDateInstance( DateFormat.SHORT ).format( new Date() ).replace( '/',
                                                                                                       '-' );

        Iterator nameIter = this.loggers.keySet().iterator();
        String   eachName = null;
        Logger   eachLogger = null;

        while ( nameIter.hasNext() )
        {
            eachName = (String) nameIter.next();
            eachLogger = (Logger) this.loggers.get( eachName );

            try
            {
                eachLogger.setLog( new File( this.channelLogDir,
                                             baseName + "." + eachName ) );
            }
            catch (IOException e)
            {
                System.err.println( e.getLocalizedMessage() );
            }
        }
    }

    public void stopLogs()
    {
        Iterator loggerIter = this.loggers.values().iterator();
        Logger   eachLogger = null;

        while ( loggerIter.hasNext() )
        {
            eachLogger = (Logger) loggerIter.next();

            try
            {
                eachLogger.setLog( null );
            }
            catch (IOException e)
            {
                System.err.println( e.getLocalizedMessage() );
            }
        }
    }

    public void initialize() throws JerkException
    {
        System.err.println( "initilaize for " + this.channel + " with " + this.props );

        this.channelLogDir = new File( this.logDir,
                                       this.channel.getName().substring( 1 ) );


        if ( ! this.channelLogDir.exists() )
        {
            this.channelLogDir.mkdirs();
        }

        if ( this.props.getProperty( "txt" ).equals( "on" ) )
        {
            addLogger( "txt",
                       new TextLogger() );
        }
        if ( this.props.getProperty( "html" ).equals( "on" ) )
        {
            addLogger( "html",
                       new HtmlLogger() );
        }

        startLogs();
    }

    public void shutdown()
    {
        stopLogs();
    }

    public void acceptMessage(Message message) throws JerkException
    {
        if ( message.getPayload().toLowerCase().startsWith( "[off]" ) )
        {
            return;
        }

        Iterator loggerIter = this.loggers.values().iterator();
        Logger   eachLogger = null;

        while ( loggerIter.hasNext() )
        {
            eachLogger = (Logger) loggerIter.next();

            try
            {
                eachLogger.logMessage( message.getDate(),
                                       getNick( message ),
                                       message.getPayload() );
            }
            catch (IOException e)
            {
                System.err.println( e.getLocalizedMessage() );
            }
        }
    }

    protected String getNick(Message message)
    {
        String source = message.getSource();

        return source.substring( 0,
                                 source.indexOf( "!" ) );
                                              
    }
}
