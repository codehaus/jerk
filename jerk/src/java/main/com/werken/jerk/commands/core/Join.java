package com.werken.jerk.commands.core;

import com.werken.jerk.Server;
import com.werken.jerk.Channel;
import com.werken.jerk.Command;
import com.werken.jerk.Message;
import com.werken.jerk.Tokenizer;
import com.werken.jerk.JerkException;

import java.io.IOException;

public class Join implements Command
{
    public void perform(Message message) throws IOException
    {
        if ( ! message.isPrivate() )
        {
            message.reply( "this command must be /msg'd to me privately" );
            return;
        }

        Tokenizer tokens = new Tokenizer( message.getPayload() );

        String join = tokens.consumeNextToken();
        String channelName = null;
        Channel channel = null;

        Server server = message.getServer();

        while ( ! ( channelName = tokens.consumeNextToken() ).equals( "" ) )
        {
            if ( ! channelName.startsWith( "#" ) )
            {
                channelName = "#" + channelName;
            }

            try
            {
                server.join( channelName );
                message.reply( "joined " + channelName );
            }
            catch (JerkException e)
            {
                message.reply( "unable to join " + channelName + " :: " + e.getLocalizedMessage() );
            }
        }
    }
}
