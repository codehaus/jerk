package com.werken.jerk;

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

import java.util.Properties;

/** Interface for a service.
 *
 *  @author <a href="mailto:bob@eng.werken.com">bob mcwhirter</a>
 */
public interface Service
{
    /** Initialize the jerk-wide service.
     *
     *  @param jerk The jerk.
     *  @param serviceProps Service properties.
     *  @param chanServiceProps Channel specific service props.
     *
     *  @throws JerkException If an error occurs while attempting
     *          to perform initialization.
     */
    void initialize(Jerk jerk,
                    Properties serviceProps,
                    Properties chanServiceProps) throws JerkException;

    /** Shutdown the jerk-wide service.
     */
    void shutdown();

    /** Start the service on a channel.
     *
     *  @param channel The channel.
     *
     *  @return The newly bound channel service.
     *
     *  @throws JerkException If the service encounters errors while
     *          attempting to start.
     */
    ChannelService startChannelService(Channel channel) throws JerkException;

    /** Stop the service on a channel.
     *
     *  @param channel The channel.
     *
     *  @throws JerkException If the service encounters errors while
     *          attempting to stop.
     */
    void stopChannelService(Channel channel) throws JerkException;

    /** Retrieve this service's global command.
     *
     *  @return The command for this service, or <code>null</code>
     *          if this service does not intend to register a command.
     */
    Command getCommand();
}
