package com.werken.jerk;

public class Tokenizer
{
    private String text;

    int cur;

    public Tokenizer(String text)
    {
        this.text = text;

        this.cur = 0;
    }

    public String peekNextToken()
    {
        int tmp = this.cur;

        String token = consumeNextToken();

        this.cur = tmp;

        return token;
    }

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

    public String consumeRest()
    {
        String rest = this.text.substring( cur );

        cur = this.text.length();

        return rest;
    }
}
