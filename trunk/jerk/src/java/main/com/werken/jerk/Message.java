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
import java.util.Date;

/** A public or private irc message.
 *
 *  @author <a href="mailto:bob@eng.werken.com">bob mcwhirter</a>
 */
public class Message
{
    // ------------------------------------------------------------
    //     Instance members
    // ------------------------------------------------------------

    /** The server. */
    private Server server;

    /** The source of the message. */
    private String source;

    /** The channel of the message (null if private). */
    private Channel channel;

    /** The message payload. */
    private String payload;

    /** The message timestamp. */
    private Date date;

    // ------------------------------------------------------------
    //     Constructors
    // ------------------------------------------------------------

    /** Construct.
     *
     *  @param server The server.
     *  @param source The source.
     *  @param channel The channel.
     *  @param payload The payload.
     */
    Message(Server server,
            String source,
            Channel channel,
            String payload)
    {
        this.server  = server;

        this.source  = source;
        this.channel = channel;
        this.payload = payload;
        this.date    = new Date();
    }

    // ------------------------------------------------------------
    //     Instance methods
    // ------------------------------------------------------------

    /** Retrieve the server.
     *
     *  @return The server.
     */
    public Server getServer()
    {
        return this.server;
    }

    /** Retrieve the source.
     *
     *  @return The source.
     */
    public String getSource()
    {
        return this.source;
    }

    /** Retrieve the channel.
     *
     *  @return The channel if this message was public on a channel,
     *          otherwise <code>null</code> which indicates a private
     *          message.
     */
    public Channel getChannel()
    {
        return this.channel;
    }

    /** Retrieve the payload.
     *
     *  @return The payload.
     */
    public String getPayload()
    {
        return this.payload;
    }

    /** Retrieve the date timestamp.
     *
     *  @return The date timestamp.
     */
    public Date getDate()
    {
        return this.date;
    }

    /** Set the payload.
     *
     *  @param payload The payload.
     */
    void setPayload(String payload)
    {
        this.payload = payload;
    }

    /** Determine if this message was public or private.
     *
     *  @return <code>true</code> if this message was private,
     *          otherwise <code>false</code>.
     */
    public boolean isPrivate()
    {
        return ( this.channel == null );
    }

    /** Send a reply to this message.
     *
     *  @param message The reply text.
     *
     *  @throws IOException If an IO error occurs.
     */
    public void reply(String message) throws IOException
    {
        if ( this.channel == null )
        {
            this.server.serverWrite( "PRIVMSG " + getSource() + " :" + message );
        }
        else
        {
            this.server.serverWrite( "PRIVMSG " + this.channel.getName() + " :" + message );
        }
    }

    /** Produce a textual representation suitable for debugging.
     *
     *  @return A debug string.
     */
    public String toString()
    {
        return "[Message: source=" + getSource()
            + "; channel=" + getChannel()
            + "; payload=" + getPayload()
            + "]";
    }

    /** Parse a raw server message into a message object.
     *
     *  @param server The server.
     *  @param text The raw PRIVMSG message text.
     *
     *  @return The parsed message object.
     */
    public static Message parse(Server server,
                                String text)
    {
        Tokenizer tokens  = new Tokenizer( text );
        Message   message = null;

        String source      = tokens.consumeNextToken();
        String privmsg     = tokens.consumeNextToken();
        String destination = tokens.consumeNextToken();
        String payload     = tokens.consumeRest();

        source  = source.substring( 1 );
        payload = payload.substring( 1 );

        Channel channel = server.getChannel( destination );
        
        message = new Message( server,
                               source,
                               channel,
                               payload );

        return message;
    }

}
