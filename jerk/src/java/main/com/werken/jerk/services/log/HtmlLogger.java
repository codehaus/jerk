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
import java.io.FileWriter;
import java.io.RandomAccessFile;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.PrintWriter;
import java.io.InputStreamReader;
import java.io.IOException;

import java.util.Date;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

//
// NOTE: Replace w/StringEscapeUtils once the commons-lang SNAPSHOT is updated
//
import org.apache.commons.lang.StringUtils;

import com.werken.jerk.util.StringValueParser;

/** Logger for HTML logs.
 *
 *  @author <a href="mailto:bob@eng.werken.com">bob mcwhirter</a>
 */
public class HtmlLogger extends AbstractLogger
{
    private static final Log log = LogFactory.getLog(HtmlLogger.class);
    
    // ------------------------------------------------------------
    //     Instance members
    // ------------------------------------------------------------
    
    /** The log sink. */
    private RandomAccessFile out;
    
    /** Insertion point. */
    private long insertion;
    
    /** Even/odd line for display. */
    private boolean isOdd;
    
    /** Format to use for timestamps.  see java.text.SimpleDateFormat */
    private String tsFormat = "hh:mm:ssa";
    
    /** String value parser */
    private StringValueParser valueParser = new StringValueParser();
    
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
        log.debug("Opening log file: " + logFile);
        
        boolean isNew = true;
        insertion = 0;
        
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
        else
        {
            // Its a new file, update the latest file
            File latestFile = new File(logFile.getParentFile(), "latest.html");
            log.debug("Writing new latest file: " + latestFile);
            PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(latestFile)));
            
            try {
                writer.println("<html><head><script language=\"javascript\">");
                writer.println("<!--");
                writer.println("document.location.replace(\"" + logFile.getName() + "\");");
                writer.println("//-->");
                writer.println("</script></head>");
                writer.println("<body>");
                writer.println("Redirecting your browser to the latest conversation log. ");
                writer.println("If you are not redirected click ");
                writer.println("<a href=\"" + logFile.getName() + "\">here</a>.");
                writer.println("</body></html>");
                writer.flush();
            }
            finally {
                writer.close();
            }
        }
        
        this.out = new RandomAccessFile( this.logFile, "rw" );
        
        if ( isNew )
        {
            dumpProlog();
        }
        else
        {
            this.out.seek( this.insertion );
            StringBuffer buff = new StringBuffer();
            buff.append("<tr><td valign='top' class='time'>")
                .append(formatTimestamp( new Date() ))
                .append("</td><td valign='top' colspan='2' class='event'>")
                .append("log continues")
                .append("</td></tr>\n");
            
            this.out.writeBytes( buff.toString() );
            this.insertion += buff.length();
        }
        
        dumpEpilog();
    }
    
    /** Close the log.
     *
     *  @throws IOException If an IO error occurs.
     */
    protected void closeLog() throws IOException
    {
        log.debug("Closing log file: " + logFile);
        
        out.seek( this.insertion );
        
        StringBuffer buff = new StringBuffer();
        buff.append("<tr><td valign='top' class='time'>")
            .append(formatTimestamp( new Date() ))
            .append("</td><td valign='top' colspan='2' class='event'>" )
            .append("log stops")
            .append("</td></tr>\n");
            
        out.writeBytes( buff.toString() );
        out.close();
    }
    
    /** Dump the prolog to the file.
     *
     *  @throws IOException If an IO error occurs.
     */
    protected void dumpProlog() throws IOException
    {
        InputStream in = getClass().getClassLoader().getResourceAsStream( "com/werken/jerk/services/log/prolog" );
        BufferedReader reader = new BufferedReader( new InputStreamReader( in ) );
        
        valueParser.setVariable("DATE", DateFormat.getDateInstance().format( new Date() ));
        
        try {
            String line = null;
            while ( ( line = reader.readLine() ) != null )
            {
                line = valueParser.parse(line);
                
                out.writeBytes( line + "\n" );
                insertion += line.length() + 1;
            }
        }
        finally {
            reader.close();
        }
        
        StringBuffer buff = new StringBuffer();
        buff.append("<tr><td valign='top' class='time'>")
            .append(formatTimestamp( new Date() ))
            .append("</td><td valign='top' colspan='2' class='event'>")
            .append("log begins")
            .append("</td></tr>\n");
        
        out.writeBytes( buff.toString() );
        insertion += buff.length();
    }
    
    /** Dump the epilog to the file.
     *
     *  @throws IOException If an IO error occurs.
     */
    public void dumpEpilog() throws IOException
    {
        InputStream in = getClass().getClassLoader().getResourceAsStream( "com/werken/jerk/services/log/epilog" );
        BufferedReader reader = new BufferedReader( new InputStreamReader( in ) );
        
        try {
            String line = null;
            while ( ( line = reader.readLine() ) != null )
            {
                out.writeBytes( line + "\n" );
            }
        }
        finally {
            reader.close();
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
        //
        // NOTE: Replace w/StringEscapeUtils once the commons-lang SNAPSHOT is updated
        //
        line = StringUtils.replace(line, "&", "&amp;");
        line = StringUtils.replace(line, "<", "&lt;");
        line = StringUtils.replace(line, ">", "&gt;");
        
        return line;
    }

    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
    //     com.werken.jerk.services.log.Logger
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 

    public void setChannel(Channel channel)
    {
        super.setChannel(channel);
        valueParser.setVariable("channel", channel);
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
        this.out.seek( this.insertion );

        String evenOdd = "odd";

        if ( ! isOdd )
        {
            evenOdd = "even";
        }
        
        StringBuffer buff = new StringBuffer();
        buff.append("<tr><td valign='top' class='time'>")
            .append(formatTimestamp( new Date() ))
            .append("</td><td valign='top' class='nick'>").append(nick)
            .append("</td><td valign='top' class='text-").append(evenOdd).append("'>")
            .append(sanitize( message ))
            .append("</td></tr>\n");

        this.out.writeBytes( buff.toString() );
        this.insertion += ( buff.length() );
        
        isOdd = ! isOdd;
        
        dumpEpilog();
    }
    
    private SimpleDateFormat dateFormat;
    
    private String formatTimestamp(final Date timestamp)
    {
        if (dateFormat == null) {
            SimpleDateFormat df = (SimpleDateFormat)DateFormat.getDateInstance(DateFormat.SHORT);
            df.applyPattern(tsFormat);
            dateFormat = df;
        }
        
        return dateFormat.format(timestamp);
    }
}
