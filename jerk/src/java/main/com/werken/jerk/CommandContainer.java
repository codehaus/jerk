package com.werken.jerk;

import org.apache.commons.beanutils.ConvertingWrapDynaBean;

public class CommandContainer
{
    private Class    commandClass;
    private String[] paramList;

    public CommandContainer(Class commandClass,
                            String[] paramList)
    {
        this.commandClass = commandClass;
        this.paramList    = paramList;
    }

    public Class getCommandClass()
    {
        return this.commandClass;
    }

    public String[] getParamList()
    {
        return this.paramList;
    }

    public Command newCommand(String msg)
        throws InstantiationException, IllegalAccessException
    {
        Command commandObj = (Command) this.commandClass.newInstance();

        ConvertingWrapDynaBean command = new ConvertingWrapDynaBean( commandObj );

        Tokenizer msgText = new Tokenizer( msg );

        String param = null;

        for ( int i = 0 ; i < paramList.length ; ++i )
        {
            param = paramList[i];

            if ( param.endsWith( "*" ) )
            {
                param = param.substring( 0,
                                         param.length()-1 );

                command.set( param,
                             msgText.consumeRest() );

                break;
            }
            else
            {
                command.set( param,
                             msgText.consumeNextToken() );
            }
        }

        return commandObj;
    }
}
