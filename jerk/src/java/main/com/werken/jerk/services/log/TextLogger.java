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
import java.io.PrintStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.text.DateFormat;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/** Logger for text logs.
 *
 *  @author <a href="mailto:bob@eng.werken.com">bob mcwhirter</a>
 */
public class TextLogger extends AbstractLogger
{
    private static final Log log = LogFactory.getLog(TextLogger.class);
    
    // ------------------------------------------------------------
    //     Instance members
    // ------------------------------------------------------------

    /** The log sink. */
    private PrintStream out;

    // ------------------------------------------------------------
    //     Constructors
    // ------------------------------------------------------------

    /** Construct.
     */
    public TextLogger()
    {
        // intentionally left blank.
    }

    // ------------------------------------------------------------
    //     Instance methods.
    // ------------------------------------------------------------

    /** Open the log.
     *
     *  @throws IOException If an IO error occurs.
     */
    protected void openLog() throws IOException
    {
        log.debug("Opening log file: " + logFile);
        
        out = new PrintStream( new FileOutputStream( logFile.getPath(), true ) );

        out.println( DateFormat.getTimeInstance().format( new Date() ) + "## log begins" );
    }

    /** Close the log.
     *
     *  @throws IOException If an IO error occurs.
     */
    protected void closeLog() throws IOException
    {
        log.debug("Closing log file: " + logFile);
        out.println( DateFormat.getTimeInstance().format( new Date() ) + "## log ends" );
        out.close();
    }

    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
    //     com.werken.jerk.services.log.TextLogger
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 

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
        this.out.println( DateFormat.getTimeInstance().format( timestamp )
                          + " <" + nick + "> "
                          + message );
    }
}
