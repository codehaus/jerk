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

/** Binding of the log service to a channel.
 *
 *  @author <a href="mailto:bob@eng.werken.com">bob mcwhirter</a>
 */
public class LogChannelService implements ChannelService
{
    // ------------------------------------------------------------
    //     Instance members
    // ------------------------------------------------------------

    /** The channel. */
    private Channel channel;

    /** Config props. */
    private Properties props;

    /** Log directory. */
    private File logDir;

    /** Channe log directory. */
    private File channelLogDir;

    /** Loggers. */
    private Map loggers;

    // ------------------------------------------------------------
    //     Constructors
    // ------------------------------------------------------------

    /** Construct.
     *
     *  @param channel The channel.
     *  @param props Config props.
     *  @param logDir The main log directory.
     */
    LogChannelService(Channel channel,
                      Properties props,
                      File logDir)
    {
        this.channel = channel;
        this.props   = props;
        this.logDir  = logDir;
        this.loggers = new HashMap();
    }

    // ------------------------------------------------------------
    //     Instance methods
    // ------------------------------------------------------------
    
    /** Roll all logs.
     */
    public void rollLogs()
    {
        stopLogs();
        startLogs();
    }

    /** Add a logger.
     *
     *  @param name The name.
     *  @param logger The logger.
     */
    public void addLogger(String name,
                          Logger logger)
    {
        logger.setChannel( this.channel );
        this.loggers.put( name,
                          logger );
    }

    /** Start all logs.
     */
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

    /** Stop all logs.
     */
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

    /** Extract the nickname from a message.
     *
     *  @param message The message to parse.
     *
     *  @return The nickname of the sender.
     */
    protected String getNick(Message message)
    {
        String source = message.getSource();

        return source.substring( 0,
                                 source.indexOf( "!" ) );
                                              
    }

    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
    //     com.werken.jerk.ChannelSerivce
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 

    /** Initialize the channel service.
     *
     *  @throws JerkException If an error occurs while attempting
     *          to perform initialization.
     */
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

    /** Shutdown the channel service.
     */
    public void shutdown()
    {
        stopLogs();
    }

    /** Accept a message in this service.
     *
     *  @param message The message to accept.
     *
     *  @throws JerkException If an error occurs while attempting
     *          to process the message.
     */
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
}
