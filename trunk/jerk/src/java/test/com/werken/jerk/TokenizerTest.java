package com.werken.jerk;

import junit.framework.TestCase;

public class TokenizerTest extends TestCase
{
    public TokenizerTest(String name)
    {
        super( name );
    }

    public void setUp() throws Exception
    {
        super.setUp();
    }

    public void tearDown() throws Exception
    {
        super.tearDown();
    }

    public void testPeekNextToken_single()
    {
        Tokenizer tokenizer = new Tokenizer( "foo" );

        assertEquals( "foo",
                      tokenizer.peekNextToken() );

        assertEquals( "foo",
                      tokenizer.peekNextToken() );

        assertEquals( "foo",
                      tokenizer.peekNextToken() );
    }

    public void testConsumeNextToken_single()
    {
        Tokenizer tokenizer = new Tokenizer( "foo" );

        assertEquals( "foo",
                      tokenizer.consumeNextToken() );

        assertEquals( "",
                      tokenizer.consumeRest() );
    }

    public void testPeekConsumeNextToken_Multi()
    {
        Tokenizer tokenizer = new Tokenizer( "foo bar" );

        assertEquals( "foo",
                      tokenizer.peekNextToken() );

        assertEquals( "foo",
                      tokenizer.peekNextToken() );

        assertEquals( "foo",
                      tokenizer.consumeNextToken() );

        assertEquals( "bar",
                      tokenizer.peekNextToken() );

        assertEquals( "bar",
                      tokenizer.peekNextToken() );

        assertEquals( "bar",
                      tokenizer.consumeNextToken() );

        assertEquals( "",
                      tokenizer.consumeRest() );
    }
}


