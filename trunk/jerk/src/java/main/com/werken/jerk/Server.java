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

import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.net.Socket;
import java.util.Map;
import java.util.HashMap;
import java.util.Collection;

/** A jerk-connected IRC server.
 *
 *  @author <a href="mailto:bob@eng.werken.com">bob mcwhirter</a>
 */
public class Server implements Runnable
{
    // ------------------------------------------------------------
    //     Instance members
    // ------------------------------------------------------------

    /** The jerk. */
    private Jerk jerk;

    /** The server connection socket. */
    private Socket socket;

    /** Input reading. */
    private BufferedReader in;

    /** Output writing. */
    private BufferedWriter out;

    /** Should run flag? */
    private boolean shouldRun;

    /** Address of the server. */
    private String address;

    /** Port of the server. */
    private int port;

    /** The nickname to user. */
    private String nick;

    /** Active channels. */
    private Map channels;

    // ------------------------------------------------------------
    //     Constructors
    // ------------------------------------------------------------

    /** Construct.
     *
     *  @param jerk The jerk.
     *  @param address The server address.
     *  @param port The server port.
     *  @param nick The nickname.
     */
    public Server(Jerk jerk,
                  String address,
                  int port,
                  String nick)
    {
        this.jerk      = jerk;
        this.address   = address.toLowerCase();
        this.port      = port;
        this.nick      = nick;

        this.channels  = new HashMap();
        this.shouldRun = true;
    }

    // ------------------------------------------------------------
    //     Instance methods
    // ------------------------------------------------------------

    /** Retrieve the address of the server.
     *
     *  @return The address.
     */
    public String getAddress()
    {
        return this.address;
    }

    /** Retrieve the port of the server.
     *
     *  @return The port.
     */
    public int getPort()
    {
        return this.port;
    }

    /** Retrieve the nickname of the jerk.
     *
     *  @return The nickname.
     */
    public String getNick()
    {
        return this.nick;
    }

    /** Parse the response code/command from a message.
     *
     *  @param msg The message to parse.
     *
     *  @return The response code/command.
     */
    protected String getResponseCode(String msg)
    {
        int cur = 0;
        int space = 0;

        space = msg.indexOf( " ",
                             cur );

        cur = space + 1;

        space = msg.indexOf( " ",
                             cur );

        return msg.substring( cur,
                              space );
    }

    /** Parse the response message.
     *
     *  @param msg The message to parse.
     *
     *  @return The response message.
     */
    protected String getResponseMessage(String msg)
    {
        int cur = 0;
        int space = 0;

        space = msg.indexOf( " ",
                             cur );

        cur = space + 1;

        space = msg.indexOf( " ",
                             cur );

        cur = space + 1;

        space = msg.indexOf( " ",
                             cur );

        cur = space + 1;

        return msg.substring( cur );
    }

    /** Join a channel.
     *
     *  @param name The channel to join.
     *
     *  @throws IOException If an IO error occurs.
     *  @throws JerkException If the channel is already parted or
     *          otherwise cannot be joined.
     */
    public void join(String name) throws IOException, JerkException
    {
        if ( this.channels.containsKey( name ) )
        {
            throw new JerkException( "already joined" );
        }

        Channel channel = new Channel( this,
                                       name );

        channel.join();
    }

    /** Join a channel.
     *
     *  @param channel The channel to join.
     *
     *  @throws IOException If an IO error occurs.
     *  @throws JerkException If the channel is already parted or
     *          otherwise cannot be joined.
     */
    protected void join(Channel channel) throws IOException, JerkException
    {
        if ( this.channels.containsKey( channel.getName() ) )
        {
            return;
        }

        serverWrite( "JOIN " + channel.getName() );

        String msg = null;

        while ( ( msg = serverRead() ) == null )
        {
            // keep waiting for response
        }

        String codeStr = getResponseCode( msg );

        if ( ! codeStr.toUpperCase().equals( "JOIN" ) )
        {
            int code = 200;
            
            try
            {
                code = Integer.parseInt( codeStr );
            }
            catch (NumberFormatException e)
            {
                throw new JerkException( msg );
            }
            
            if ( code >= 400
                 &&
                 code < 600 )
            {
                throw new JerkException( getResponseMessage( msg ) );
            }

            throw new JerkException( msg );
        }
        
        this.channels.put( channel.getName(),
                           channel );

        this.jerk.startServices( channel );
    }

    /** Part a channel.
     *
     *  @param name The channel to join.
     *
     *  @throws IOException If an IO error occurs.
     *  @throws JerkException If the channel is already parted or
     *          otherwise cannot be parted.
     */
    public void part(String name) throws IOException, JerkException
    {
        Channel channel = getChannel( name );

        if ( channel == null )
        {
            throw new JerkException( "not joined" );
        }

        channel.part();
    }

    /** Part a channel.
     *
     *  @param channel The channel to part.
     *
     *  @throws IOException If an IO error occurs.
     *  @throws JerkException If the channel is already parted or
     *          otherwise cannot be parted.
     */
    protected void part(Channel channel) throws IOException, JerkException
    {
        if ( ! this.channels.containsKey( channel.getName() ) )
        {
            return;
        }

        serverWrite( "PART " + channel.getName() + " :part");

        String msg = null;

        while ( ( msg = serverRead() ) == null )
        {
            // keep waiting for response
        }

        String codeStr = getResponseCode( msg );

        if ( ! codeStr.toUpperCase().equals( "PART" ) )
        {
            int code = 200;
            
            try
            {
                code = Integer.parseInt( codeStr );
            }
            catch (NumberFormatException e)
            {
                throw new JerkException( msg );
            }
            
            if ( code >= 400
                 &&
                 code < 600 )
            {
                throw new JerkException( getResponseMessage( msg ) );
            }
        }

        channels.remove( channel.getName() ); 
    }

    /** Retrieve a channel.
     *
     *  @param name The name of the channel.
     *
     *  @return The named channel or <code>null</code> if
     *          this server is not actively connected to
     *          the named channel.
     */
    public Channel getChannel(String name)
    {
        return (Channel) this.channels.get( name );
    }

    /** Retrieve all actively connected channels.
     *
     *  @return A collection of all actively connected channels.
     */
    public Collection getChannels()
    {
        return this.channels.values();
    }

    /** Start a service on a channel.
     *
     *  @param channel The channel.
     *  @param name The service name.
     *
     *  @return The newly created channel service.
     *
     *  @throws JerkException If the service is undefined, already
     *          started or cannot be started.
     */
    public ChannelService startChannelService(Channel channel,
                                              String name) throws JerkException
    {
        return this.jerk.startChannelService( channel,
                                              name );
    }

    /** Stop a service on a channel.
     *
     *  @param channel The channel.
     *  @param name The service name.
     *
     *  @throws JerkException If the service is undefined, not
     *          started or cannot be stopped.
     */
    public void stopChannelService(Channel channel,
                                   String name) throws JerkException
    {
        this.jerk.stopChannelService( channel,
                                      name );
    }

    /** Connect to this IRC server.
     *
     *  @throws IOException If an IO error occurs while 
     *          attempting to connect.
     */
    public void connect() throws IOException
    {
        Thread thread = new Thread( this );

        this.shouldRun = true;

        System.err.println( "connect to " + getAddress() + ":" + getPort() );

        this.socket = new Socket( getAddress(),
                                  getPort() );

        this.in  = new BufferedReader( new InputStreamReader( this.socket.getInputStream() ) );
        this.out = new BufferedWriter( new OutputStreamWriter( this.socket.getOutputStream() ) );

        Runtime.getRuntime().addShutdownHook(
            new Thread()
            {
                public void run() 
                {
                    try
                    {
                        disconnect();
                    }
                    catch (IOException e)
                    {
                        System.err.println( e.getLocalizedMessage() );
                    }
                }
            }
            );
        thread.start();
    }

    /** Disconnect from this IRC server.
     *
     *  @throws IOException If an IO error occurs while 
     *          attempting to disconnect.
     */
    public void disconnect() throws IOException
    {
        this.shouldRun = false;
    }

    /** Service a message.
     *
     *  @param msgText The message text.
     *
     *  @throws IOException If an IO error occurs.
     */
    protected void service(String msgText) throws IOException
    {
        if ( msgText.substring(0,4).equalsIgnoreCase( "ping" ) )
        {
            pingPong(msgText);
            return;
        }

        int space1 = msgText.indexOf( " " );

        if ( space1 > 0 )
        {
            int space2 = msgText.indexOf( " ",
                                          space1 + 1 );

            if ( space2 > 0 )
            {
                if ( msgText.substring( space1 + 1,
                                        space2 ).equalsIgnoreCase( "PRIVMSG" ) )
                {
                    Message message = Message.parse( this,
                                                     msgText );

                    if ( message != null )
                    {
                        dispatchMessage( message );
                    }
                    return;
                }
            }
        }
    }

    /** Dispatch a message.
     *
     *  @param message The message.
     *
     *  @throws IOException If an IO error occurs.
     */
    protected void dispatchMessage(Message message) throws IOException
    {
        if ( message.isPrivate() )
        {
            dispatchPrivateMessage( message );
        }
        else
        {
            Tokenizer tokens = new Tokenizer( message.getPayload() );

            if ( tokens.peekNextToken().toLowerCase().startsWith( getNick().toLowerCase() ) )
            {
                // public, directed to the jerk.
                dispatchPublicMessage( message );
            }
            else
            {
                // completely public.
                message.getChannel().acceptMessage( message );
            }
        }
    }

    /** Dispatch a private message.
     *
     *  @param message The message.
     *
     *  @throws IOException If an IO error occurs.
     */
    protected void dispatchPrivateMessage(Message message) throws IOException
    {
        dispatchMessageInternal( message );
    }

    /** Dispatch a public message.
     *
     *  @param message The message.
     *
     *  @throws IOException If an IO error occurs.
     */
    protected void dispatchPublicMessage(Message message) throws IOException
    {
        Tokenizer tokens = new Tokenizer( message.getPayload() );

        String first = tokens.consumeNextToken();

        message.setPayload( tokens.consumeRest() );

        dispatchMessageInternal( message );
    }

    /** Internally dispatch a public message.
     *
     *  @param message The message.
     *
     *  @throws IOException If an IO error occurs.
     */
    protected void dispatchMessageInternal(Message message) throws IOException
    {
        Tokenizer tokens = new Tokenizer( message.getPayload() );

        Command command = this.jerk.getCommand( tokens.peekNextToken() );

        if ( command != null )
        {
            command.perform( message );
            return;
        }
    }

    /** Perform IRC protocol ping pong.
     *
     *  @param msg The ping message.
     *
     *  @throws IOException If an IO errors occurs.
     */
    protected void pingPong(String msg) throws IOException
    {
        serverWrite( "pong " + msg.substring( 5 ) );
    }

    /** Run the socket thread.
     */
    public void run()
    {
        String msg = null;

        try
        {
            serverWrite( "user " + getNick() + " 0 * : the jerk" );
            serverWrite( "nick " + getNick() );
        }
        catch (IOException e)
        {
            System.err.println( e.getLocalizedMessage() );
            return;
        }

        int multiplier = 1;

        try
        {
            while ( this.shouldRun )
            {
                try
                {
                    Thread.sleep( 100 * multiplier );
                }
                catch (InterruptedException e)
                {
                    break;
                }

                msg = serverRead();
                
                if ( msg == null )
                {
                    if ( multiplier < 5 )
                    {
                        ++multiplier;
                    }

                    continue;
                }

                multiplier = 1;
                
                service( msg );
            }

            serverWrite( "QUIT :quit" );
        }
        catch (IOException e)
        {
            System.err.println( e.getLocalizedMessage() );
        }
    }

    /** Write a bare low-level message to the server.
     *
     *  @param msg The message to write.
     *
     *  @throws IOException If an IO error occurs.
     */
    public void serverWrite(String msg) throws IOException
    {
        this.out.write( msg );
        this.out.newLine();
        this.out.flush();
    }

    /** Read a bare low-level message to the server.
     *
     *  @return The message read, or <code>null</code>
     *          if no unread message is available.
     *
     *  @throws IOException If an IO error occurs.
     */
    public String serverRead() throws IOException
    {
        if ( this.in.ready() )
        {
            return in.readLine();
        }

        return null;
    }

    /** Produce a textual representation suitable for debugging.
     *
     *  @return A debug string.
     */
    public String toString()
    {
        return "[Server: address='" + getAddress() + "'"
            + "; port='" + getPort() + "'"
            + "]";
    }
}
