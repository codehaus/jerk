package com.werken.jerk.services.log;

import com.werken.jerk.Channel;

import java.io.File;
import java.io.FileReader;
import java.io.RandomAccessFile;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.Date;
import java.text.DateFormat;

public class HtmlLogger implements Logger
{
    private File logFile;
    private Channel channel;
    private RandomAccessFile log;

    private long insertion;

    private boolean isOdd;

    public void HtmlLogger()
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
        boolean isNew = true;

        if ( this.logFile.exists() )
        {
            isNew = false;
            BufferedReader reader = new BufferedReader( new FileReader( this.logFile ) );

            String line = null;

            while ( ( line = reader.readLine() ) != null )
            {
                if ( line.indexOf( "jerk insertion point" ) >= 0 )
                {
                    break;
                }

                this.insertion += line.length() + 1;
            }

            reader.close();
        }

        this.log = new RandomAccessFile( this.logFile,
                                         "rw" );
        
        if ( isNew )
        {
            dumpProlog();
        }
        else
        {
            this.log.seek( this.insertion );
            String entry = "<tr><td valign='top' class='time'>" + DateFormat.getTimeInstance().format( new Date() )
                + "</td><td valign='top' colspan='2' class='event'>" 
                + "log continues"
                + "</td></tr>\n";
            
            this.log.writeBytes( entry );
            this.insertion += entry.length();
        }
        
        dumpEpilog();
    }
    
    protected void closeLog() throws IOException
    {
        this.log.seek( this.insertion );

        String entry = "<tr><td valign='top' class='time'>" + DateFormat.getTimeInstance().format( new Date() )
            + "</td><td valign='top' colspan='2' class='event'>" 
            + "log stops"
            + "</td></tr>\n";
            
        this.log.writeBytes( entry );
        this.log.close();
    }

    public void logMessage(Date timestamp,
                           String nick,
                           String message) throws IOException
    {
        this.log.seek( this.insertion );

        String evenOdd = "odd";

        if ( ! isOdd )
        {
            evenOdd = "even";
        }
        
        try
        {
            String entry = "<tr><td valign='top' class='time'>" + DateFormat.getTimeInstance().format( new Date() )
                + "</td><td valign='top' class='nick'>" + nick
                + "</td><td valign='top' class='text-" + evenOdd + "'>" + sanitize( message )
                + "</td></tr>\n";

            this.log.writeBytes( entry );
            
            this.insertion += ( entry.length() );
            
            isOdd = ! isOdd;
            
            dumpEpilog();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
                      
    }

    protected String substitutePrologVars(String line)
    {
        String newLine = line;

        int varLoc = line.indexOf( "$CHANNEL$" );

        if ( varLoc >= 0 )
        {
            newLine = line.substring( 0,
                                      varLoc );

            newLine += this.channel.getName();

            newLine += line.substring( varLoc + 9 );
        }

        line = newLine;

        varLoc = line.indexOf( "$DATE$" );

        if ( varLoc >= 0 )
        {
            newLine = line.substring( 0,
                                      varLoc );

            newLine += DateFormat.getDateInstance().format( new Date() );

            newLine += line.substring( varLoc + 6 );
        }

        return newLine;
    }

    public void dumpProlog() throws IOException
    {
        InputStream in = getClass().getClassLoader().getResourceAsStream( "com/werken/jerk/services/log/prolog" );
        
        BufferedReader reader = new BufferedReader( new InputStreamReader( in ) );

        String line = null;

        while ( ( line = reader.readLine() ) != null )
        {
            line = substitutePrologVars( line );
            log.writeBytes( line + "\n" );

            this.insertion += line.length() + 1;
        }

        String entry = "<tr><td valign='top' class='time'>" + DateFormat.getTimeInstance().format( new Date() )
            + "</td><td valign='top' colspan='2' class='event'>" 
            + "log begins"
            + "</td></tr>\n";

        this.log.writeBytes( entry );
        this.insertion += entry.length();
    }

    public void dumpEpilog() throws IOException
    {
        InputStream in = getClass().getClassLoader().getResourceAsStream( "com/werken/jerk/services/log/epilog" );
        
        BufferedReader reader = new BufferedReader( new InputStreamReader( in ) );
        
        String line = null;

        while ( ( line = reader.readLine() ) != null )
        {
            line += "\n";

            log.writeBytes( line );
        }
    }

    public String sanitize(String line)
    {
        int cur = 0;

        int charLoc = 0;

        String newLine = "";

        while ( cur < line.length() )
        {
            charLoc = line.indexOf( "&",
                                    cur );

            if ( charLoc >= 0 )
            {
                newLine += line.substring( cur,
                                           charLoc );

                newLine += "&amp;";

                cur = charLoc + 1;
            }
            else
            {
                break;
            }
        }


        newLine += line.substring( cur );
        line = newLine;
        newLine = "";
        cur = 0;

        while ( cur < line.length() )
        {
            charLoc = line.indexOf( "<",
                                    cur );

            if ( charLoc >= 0 )
            {
                newLine += line.substring( cur,
                                           charLoc );

                newLine += "&lt;";

                cur = charLoc + 1;
            }
            else
            {
                break;
            }
        }

        newLine += line.substring( cur );
        line = newLine;
        newLine = "";
        cur = 0;

        while ( cur < line.length() )
        {
            charLoc = line.indexOf( ">",
                                    cur );

            if ( charLoc >= 0 )
            {
                newLine += line.substring( cur,
                                           charLoc );

                newLine += "&gt;";

                cur = charLoc + 1;
            }
            else
            {
                break;
            }
        }

        newLine += line.substring( cur );
        return newLine;
    }
}
