package com.werken.jerk.services.log;

import com.werken.jerk.Command;
import com.werken.jerk.Message;
import com.werken.jerk.Tokenizer;

import java.io.IOException;

public class LogCommand implements Command
{
    private LogService service;

    public LogCommand(LogService service)
    {
        this.service = service;
    }

    public void perform(Message message) throws IOException
    {
        System.err.println( "log command: " + message );

        Tokenizer tokens = new Tokenizer( message.getPayload() );

        String log = tokens.consumeNextToken();

        String cmd = tokens.consumeNextToken();

        if ( "roll".equals( cmd ) )
        {
            this.service.rollAllLogs();
            message.reply( "rolled all logs" );
            return;
        }
        else
        {
            message.reply( "unknown log command [" + cmd + "]" );
        }
    }
}
