package com.werken.jerk.services.log;

import com.werken.jerk.Channel;

import java.io.File;
import java.io.PrintStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.text.DateFormat;

public class TextLogger implements Logger
{
    private File logFile;
    private Channel channel;
    private PrintStream log;

    public void TextLogger()
    {
        
    }

    public void setChannel(Channel channel)
    {
        this.channel = channel;
    }

    public void setLog(File logFile) throws IOException
    {
        if ( logFile == null )
        {
            closeLog();
            this.logFile = null;
            return;
        }

        else if ( this.logFile == null
                  ||
                  ! this.logFile.equals( logFile ) )
        {
            this.logFile = logFile;
            openLog();
        }
    }

    protected void openLog() throws IOException
    {
        System.err.println("opening " + logFile.getPath() );
        this.log = new PrintStream( new FileOutputStream( logFile.getPath(),
                                                          true ) );
    }

    protected void closeLog() throws IOException
    {
        this.log.close();
    }

    public void logMessage(Date timestamp,
                           String nick,
                           String message) throws IOException
    {
        this.log.println( DateFormat.getTimeInstance().format( timestamp )
                          + " <" + nick + "> "
                          + message );
    }
}
