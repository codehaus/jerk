package com.werken.jerk.commands.core;

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
import com.werken.jerk.Command;
import com.werken.jerk.Message;
import com.werken.jerk.Tokenizer;
import com.werken.jerk.JerkException;

import java.io.IOException;

/** Command to instruct the jerk to start a service.
 *
 *  @author <a href="mailto:bob@eng.werken.com">bob mcwhirter</a>
 */
public class Start implements Command
{
    /** Perform the command.
     *
     *  @param message The message trigger.
     *
     *  @throws IOException If an IO error occurs.
     */
    public void perform(Message message) throws IOException
    {
        if ( message.isPrivate() )
        {
            message.reply( "this command must be used on a channel" );
            return;
        }

        Tokenizer tokens = new Tokenizer( message.getPayload() );

        String start = tokens.consumeNextToken();
        Channel channel = message.getChannel();

        String serviceName = null;

        while ( ! ( serviceName = tokens.consumeNextToken() ).equals( "" ) )
        {
            try
            {
                channel.startService( serviceName );
                message.reply( "service " + serviceName + " started" );
            }
            catch (JerkException e)
            {
                message.reply( "unable to start service " + serviceName + " :: " + e.getLocalizedMessage() );
            }
        }
    }
}
