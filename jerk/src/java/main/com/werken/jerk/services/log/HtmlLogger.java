package com.werken.jerk.services.log;

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

/** Logger for HTML logs.
 *
 *  @author <a href="mailto:bob@eng.werken.com">bob mcwhirter</a>
 */
public class HtmlLogger implements Logger
{
    // ------------------------------------------------------------
    //     Instance members
    // ------------------------------------------------------------

    /** The logfile. */
    private File logFile;

    /** The channel. */
    private Channel channel;
    
    /** The log sink. */
    private RandomAccessFile log;

    /** Insertion point. */
    private long insertion;

    /** Even/odd line for display. */
    private boolean isOdd;

    // ------------------------------------------------------------
    //     Constructors
    // ------------------------------------------------------------

    /** Construct.
     */
    public HtmlLogger()
    {
        // intentionally left blank.
    }

    // ------------------------------------------------------------
    //     Instance methods
    // ------------------------------------------------------------

    /** Open the log.
     *
     *  @throws IOException If an IO error occurs.
     */
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
    
    /** Close the log.
     *
     *  @throws IOException If an IO error occurs.
     */
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

    /** Substitute variables in a line of text.
     *
     *  @param line The text to evaluate.
     *
     *  @return The line with substitutions performed.
     */
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

    /** Dump the prolog to the file.
     *
     *  @throws IOException If an IO error occurs.
     */
    protected void dumpProlog() throws IOException
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

    /** Dump the epilog to the file.
     *
     *  @throws IOException If an IO error occurs.
     */
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

    /** Escape HTML entities.
     *
     *  @param line The line to escape.
     *
     *  @return The escaped line.
     */
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

    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
    //     com.werken.jerk.services.log.Logger
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 

    /** Set the channel for this logger.
     *
     *  @param channel The channel.
     */
    public void setChannel(Channel channel)
    {
        this.channel = channel;
    }

    /** Set the log file.
     *
     *  <p>
     *  If <code>null</code> is passed, that signals that
     *  the current file should be closed.
     *  </p>
     *
     *  @param logFile The log file.
     *
     *  @throws IOException If an IO error occurs.
     */
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

    /** Log a message.
     *
     *  @param timestamp The timestamp.
     *  @param nick The nickname.
     *  @param message The message text.
     *
     *  @throws IOException If an IO error occurs.
     */
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


}
