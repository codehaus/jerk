package com.werken.jerk;

import java.io.IOException;
import java.util.Date;

public class Message
{
    private Server server;
    private String source;
    private Channel channel;
    private String payload;
    private Date date;

    public Message(Server server,
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

    public Server getServer()
    {
        return this.server;
    }

    public String getSource()
    {
        return this.source;
    }

    public Channel getChannel()
    {
        return this.channel;
    }

    public String getPayload()
    {
        return this.payload;
    }

    public Date getDate()
    {
        return this.date;
    }

    void setPayload(String payload)
    {
        this.payload = payload;
    }

    public boolean isPrivate()
    {
        return ( this.channel == null );
    }

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

    public String toString()
    {
        return "[Message: source=" + getSource()
            + "; channel=" + getChannel()
            + "; payload=" + getPayload()
            + "]";
    }

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
