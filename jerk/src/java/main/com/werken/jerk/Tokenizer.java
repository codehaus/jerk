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

/** Simple tokenizer.
 *
 *  @author <a href="mailto:bob@eng.werken.com">bob mcwhirter</a>
 */
public class Tokenizer
{
    // ------------------------------------------------------------
    //     Instance members
    // ------------------------------------------------------------

    /** The text to tokenize. */
    private String text;

    /** Current consumption point. */
    private int cur;

    // ------------------------------------------------------------
    //     Constructors
    // ------------------------------------------------------------

    /** Construct.
     *
     *  @param text The text to tokenize.
     */
    public Tokenizer(String text)
    {
        this.text = text;

        this.cur = 0;
    }

    // ------------------------------------------------------------
    //     Instance methods
    // ------------------------------------------------------------

    /** Peek at the next available token.
     *
     *  @return The next token or the empty string if no more
     *          tokens are available.
     */
    public String peekNextToken()
    {
        int tmp = this.cur;

        String token = consumeNextToken();

        this.cur = tmp;

        return token;
    }

    /** Consume the next available token.
     *
     *  @return The next token or the empty string if no more
     *          tokens are available.
     */
    public String consumeNextToken()
    {
        if ( cur >= this.text.length() )
        {
            return "";
        }

        int spaceLoc = this.text.indexOf( " ",
                                          cur );

        if ( spaceLoc < 0 )
        {
            return consumeRest();
        }

        String token = this.text.substring( cur,
                                            spaceLoc ).trim();

        cur = spaceLoc + 1;

        return token;
    }

    /** Consume the rest of the text.
     *
     *  @return The rest of the text or the empty string if no 
     *          more text is available.
     */
    public String consumeRest()
    {
        String rest = this.text.substring( cur );

        cur = this.text.length();

        return rest;
    }
}
