package com.werken.jerk;

import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.net.Socket;
import java.util.Map;
import java.util.HashMap;
import java.util.Collection;

public class Server implements Runnable
{
    private Jerk jerk;

    private Socket socket;
    private BufferedReader in;
    private BufferedWriter out;

    private boolean shouldRun;

    private String address;
    private int port;

    private String nick;

    private Map channels;

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

    public String getAddress()
    {
        return this.address;
    }

    public int getPort()
    {
        return this.port;
    }

    public String getNick()
    {
        return this.nick;
    }

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

    protected void join(Channel channel) throws IOException, JerkException
    {
        if ( channel.isJoined() )
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

        channel.setJoined( true );
        
        this.channels.put( channel.getName(),
                           channel );

        this.jerk.startServices( channel );
    }

    public void part(String name) throws IOException, JerkException
    {
        Channel channel = getChannel( name );

        if ( channel == null )
        {
            throw new JerkException( "not joined" );
        }

        channel.part();
    }

    protected void part(Channel channel) throws IOException, JerkException
    {
        if ( ! channel.isJoined() )
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

        channel.setJoined( false );

        channels.remove( channel.getName() ); 
    }

    public Channel getChannel(String name)
    {
        return (Channel) this.channels.get( name );
    }

    public Collection getChannels()
    {
        return this.channels.values();
    }

    public ChannelService startChannelService(Channel channel,
                                            String name) throws JerkException
    {
        return this.jerk.startChannelService( channel,
                                              name );
    }

    public void stopChannelService(Channel channel,
                                   String name) throws JerkException
    {
        this.jerk.stopChannelService( channel,
                                      name );
    }

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
                public void run() {
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

    public void disconnect() throws IOException
    {
        this.shouldRun = false;
    }

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

    protected void dispatchPrivateMessage(Message message) throws IOException
    {
        dispatchMessage_internal( message );
    }

    protected void dispatchPublicMessage(Message message) throws IOException
    {
        Tokenizer tokens = new Tokenizer( message.getPayload() );

        String first = tokens.consumeNextToken();

        message.setPayload( tokens.consumeRest() );

        dispatchMessage_internal( message );
    }

    protected void dispatchMessage_internal(Message message) throws IOException
    {
        Tokenizer tokens = new Tokenizer( message.getPayload() );

        Command command = this.jerk.getCommand( tokens.peekNextToken() );

        if ( command != null )
        {
            command.perform( message );
            return;
        }
    }

    protected void pingPong(String msg) throws IOException
    {
        serverWrite( "pong " + msg.substring( 5 ) );
    }

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

    public void serverWrite(String msg) throws IOException
    {
        this.out.write( msg );
        this.out.newLine();
        this.out.flush();
    }

    public String serverRead() throws IOException
    {
        if ( this.in.ready() )
        {
            return in.readLine();
        }

        return null;
    }

    public String toString()
    {
        return "[Server: address='" + getAddress() + "'"
            + "; port='" + getPort() + "'"
            + "]";
    }
}
