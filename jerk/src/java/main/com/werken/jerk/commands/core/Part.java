package com.werken.jerk.commands.core;

import com.werken.jerk.Server;
import com.werken.jerk.Channel;
import com.werken.jerk.Command;
import com.werken.jerk.Message;
import com.werken.jerk.Tokenizer;
import com.werken.jerk.JerkException;

import java.io.IOException;

public class Part implements Command
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
                server.part( channelName );
                message.reply( "parted " + channelName );
            }
            catch (JerkException e)
            {
                message.reply( "unable to parted " + channelName + " :: " + e.getLocalizedMessage() );
            }
        }
    }
}
