package com.werken.jerk.services.log;

/*
 $Id$

 Copyright 2002 (C) The Werken Company. All Rights Reserved.
 
 Redistribution and use of this software and associated documentation
 ("Software"), with or without modification, are permitted provided
 that the following conditions are met:

 1. Redistributions of source code must retain copyright
    statements and notices.  Redistributions must also contain a
    copy of this document.
 
 2. Redistributions in binary form must reproduce the
    above copyright notice, this list of conditions and the
    following disclaimer in the documentation and/or other
    materials provided with the distribution.
 
 3. The name "jerk" must not be used to endorse or promote
    products derived from this Software without prior written
    permission of The Werken Company.  For written permission,
    please contact bob@werken.com.
 
 4. Products derived from this Software may not be called "jerk"
    nor may "jerk" appear in their names without prior written
    permission of The Werken Company. "jerk" is a registered
    trademark of The Werken Company.
 
 5. Due credit should be given to the jerk project
    (http://jerk.werken.com/).
 
 THIS SOFTWARE IS PROVIDED BY THE WERKEN COMPANY AND CONTRIBUTORS
 ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 THE WERKEN COMPANY OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 OF THE POSSIBILITY OF SUCH DAMAGE.
 
 */

import com.werken.jerk.Jerk;
import com.werken.jerk.Channel;
import com.werken.jerk.Service;
import com.werken.jerk.ChannelService;
import com.werken.jerk.Command;
import com.werken.jerk.JerkException;

import java.io.File;
import java.util.Properties;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Calendar;
import java.util.Date;

/** Service to log channel text in multiple formats.
 *
 *  @author <a href="mailto:bob@eng.werken.com">bob mcwhirter</a>
 */
public class LogService implements Service
{
    // ------------------------------------------------------------
    //     Instance members
    // ------------------------------------------------------------

    /** The log directory. */
    private File logDir;

    /** Base properties for each channel. */
    private Properties chanServiceProps;

    /** Index of services deployed to channels. */
    private Map channelServices;

    /** Log-roller timer. */
    private Timer timer;

    // ------------------------------------------------------------
    //     Constructors
    // ------------------------------------------------------------

    /** Construct.
     */
    public LogService()
    {
        this.channelServices = new HashMap();
    }

    // ------------------------------------------------------------
    //     Instance methods.
    // ------------------------------------------------------------
    
    /** Register the log-rolling timed event.
     */
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

    /** Roll all logs.
     */
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

    /** Retrieve the root log directory.
     *
     *  @return The log directory.
     */
    protected File getLogDir()
    {
        return this.logDir;
    }

    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
    //     com.werken.jerk.Service
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 

    /** Start the service on a channel.
     *
     *  @param channel The channel.
     *
     *  @return The newly bound channel service.
     *
     *  @throws JerkException If the service encounters errors while
     *          attempting to start.
     */
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

    /** Stop the service on a channel.
     *
     *  @param channel The channel.
     *
     *  @throws JerkException If the service encounters errors while
     *          attempting to stop.
     */
    public void stopChannelService(Channel channel) throws JerkException
    {
        LogChannelService service = (LogChannelService) this.channelServices.get( channel );

        service.shutdown();

        this.channelServices.remove( service );
    }

    /** Retrieve this service's global command.
     *
     *  @return The command for this service.
     */
    public Command getCommand()
    {
        return new LogCommand( this );
    }

    /** Initialize the jerk-wide service.
     *
     *  @param jerk The jerk.
     *  @param serviceProps Service properties.
     *  @param chanServiceProps Channel specific service props.
     *
     *  @throws JerkException If an error occurs while attempting
     *          to perform initialization.
     */
    public void initialize(Jerk jerk,
                           Properties serviceProps,
                           Properties chanServiceProps) throws JerkException
    {
        System.err.println( "initializing log services" );

        System.err.println( serviceProps );

        String logDirName = serviceProps.getProperty( "dir" );

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

    /** Shutdown the jerk-wide service.
     */
    public void shutdown()
    {
        System.err.println( "shutting down log services" );
    }
}


