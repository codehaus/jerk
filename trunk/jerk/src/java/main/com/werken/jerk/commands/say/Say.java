package com.werken.jerk.commands.say;

import com.werken.jerk.Server;
import com.werken.jerk.Command;
import com.werken.jerk.Message;
import com.werken.jerk.Tokenizer;
import com.werken.jerk.JerkException;

import java.io.IOException;

public class Say implements Command
{
    public void perform(Message message) throws IOException
    {
        if ( ! message.isPrivate() )
        {
            message.reply( "this command must be /msg'd to me privately" );
            return;
        }

        Tokenizer tokens = new Tokenizer( message.getPayload() );

        String say         = tokens.consumeNextToken();
        String destination = tokens.consumeNextToken();
        String text        = tokens.consumeRest();

        Server server = message.getServer();

        server.serverWrite( "PRIVMSG " + destination + " :" + text );

    }
}
