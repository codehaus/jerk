package com.werken.jerk;

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

import java.io.IOException;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.Iterator;

/** An IRC channel on a <code>Server</code>.
 *
 *  @author <a href="mailto:bob@eng.werken.com">bob mcwhirter</a>
 */
public class Channel 
{
    // ------------------------------------------------------------
    //     Instance members
    // ------------------------------------------------------------

    /** The server. */
    private Server server;

    /** Channel name. */
    private String name;

    /** Currently enabled services. */
    private Map services;

    // ------------------------------------------------------------
    //     Constructors
    // ------------------------------------------------------------

    /** Construct.
     *
     *  @param server The server.
     *  @param name The channel name.
     */
    Channel(Server server,
            String name)
    {
        this.server = server;
        this.name   = name.toLowerCase();

        this.services = new HashMap();
    }

    // ------------------------------------------------------------
    //     Instance methods
    // ------------------------------------------------------------

    /** Start a service.
     *
     *  @param name The name of the service to start.
     * 
     *  @throws IOException If an IO error occurs.
     *  @throws JerkException If the service cannot be found, is already
     *          running, or encounters an error while attempting to start.
     */
    public void startService(String name) throws IOException, JerkException
    {
        if ( this.services.containsKey( name ) )
        {
            throw new JerkException( "service " + name + " already started" );
        }

        ChannelService service = getServer().startChannelService( this,
                                                                  name );

        this.services.put( name,
                           service );
    }

    /** Stop a service.
     *
     *  @param name The name of the service to Stop.
     * 
     *  @throws IOException If an IO error occurs.
     *  @throws JerkException If the service cannot be found, is not
     *          running, or encounters an error while attempting to stop.
     */
    public void stopService(String name) throws IOException, JerkException
    {
        if ( ! this.services.containsKey( name ) )
        {
            throw new JerkException( "service " + name + " not started" );
        }

        getServer().stopChannelService( this,
                                        name );

        this.services.remove( name );
    }

    /** Retrieve this channel's server.
     *
     *  @return The server.
     */
    protected Server getServer()
    {
        return this.server;
    }

    /** Join this channel.
     *
     *  @throws IOException If an IO error occurs.
     *  @throws JerkException If the channel is already joined.
     */
    void join() throws IOException, JerkException
    {
        this.server.join( this );
    }

    /** Part this channel.
     *
     *  @throws IOException If an IO error occurs.
     *  @throws JerkException If the channel is already parted.
     */
    void part() throws IOException, JerkException
    {
        this.server.part( this );
    }

    /** Retrieve the name of this channel.
     *
     *  @return The name.
     */
    public String getName()
    {
        return this.name;
    }

    /** Accept a message in this channel.
     *
     *  @param message The message to accept.
     *
     *  @throws IOException If an IO errors occurs.
     */
    public void acceptMessage(Message message) throws IOException
    {
        ChannelService service = null;

        Set names = this.services.keySet();

        Iterator serviceIter = this.services.values().iterator();

        while ( serviceIter.hasNext() )
        {
            service = (ChannelService) serviceIter.next();

            try
            {
                service.acceptMessage( message );
            }
            catch (JerkException e)
            {
                System.err.println( e.getLocalizedMessage() );
            }
        }
    }

    /** Write a message to this channel.
     *
     *  @param message The message text.
     *
     *  @throws IOException If an IO error occurs.
     */
    public void write(String message) throws IOException
    {
        this.server.serverWrite( "PRIVMSG " + getName() + " :" + message );
    }
}
