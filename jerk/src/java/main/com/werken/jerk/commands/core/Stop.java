package com.werken.jerk.commands.core;

import com.werken.jerk.Server;
import com.werken.jerk.Channel;
import com.werken.jerk.Command;
import com.werken.jerk.Message;
import com.werken.jerk.Tokenizer;
import com.werken.jerk.JerkException;

import java.io.IOException;

public class Stop implements Command
{
    public void perform(Message message) throws IOException
    {
        if ( message.isPrivate() )
        {
            message.reply( "this command must be used on a channel" );
            return;
        }

        Tokenizer tokens = new Tokenizer( message.getPayload() );

        String Stop = tokens.consumeNextToken();
        Channel channel = message.getChannel();

        String serviceName = null;

        while ( ! ( serviceName = tokens.consumeNextToken() ).equals( "" ) )
        {
            try
            {
                channel.stopService( serviceName );
                message.reply( "service " + serviceName + " stopped" );
            }
            catch (JerkException e)
            {
                message.reply( "unable to stop service " + serviceName + " :: " + e.getLocalizedMessage() );
            }
        }
    }
}
