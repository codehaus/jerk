package com.werken.jerk;

import java.io.IOException;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.Iterator;

public class Channel 
{
    private Server server;
    private String name;
    private boolean joined;

    private Map services;

    Channel(Server server,
            String name)
    {
        this.server = server;
        this.name   = name.toLowerCase();

        this.services = new HashMap();

        joined = false;
    }

    public void startService(String name) throws IOException, JerkException
    {
        if ( this.services.containsKey( name ) )
        {
            throw new JerkException( "service " + name + " already started" );
        }

        ChannelService service = getServer().startChannelService( this,
                                                                  name );

        this.services.put( name,
                           service );
    }

    public void stopService(String name) throws IOException, JerkException
    {
        if ( ! this.services.containsKey( name ) )
        {
            throw new JerkException( "service " + name + " not started" );
        }

        getServer().stopChannelService( this,
                                        name );

        this.services.remove( name );
    }

    protected Server getServer()
    {
        return this.server;
    }

    void setJoined(boolean joined)
    {
        this.joined = joined;
    }

    public boolean isJoined()
    {
        return this.joined;
    }

    public void join() throws IOException, JerkException
    {
        this.server.join( this );
    }

    public void part() throws IOException, JerkException
    {
        this.server.part( this );
    }

    public String getName()
    {
        return this.name;
    }

    public void acceptMessage(Message message) throws IOException
    {
        ChannelService service = null;

        Set names = this.services.keySet();

        /*
        Iterator nameIter = names.iterator();
        String   eachName = null;

        while ( nameIter.hasNext() )
        {
            eachName = (String) nameIter.next();

            if ( message.getExtra().startsWith( eachName + "-jerk:" ) )
            {
                service = (ChannelService) this.services.get( eachName );

                service.acceptMessage( message.getExtra() );
            }
        }
        */

        Iterator serviceIter = this.services.values().iterator();

        while ( serviceIter.hasNext() )
        {
            service = (ChannelService) serviceIter.next();

            try
            {
                service.acceptMessage( message );
            }
            catch (JerkException e)
            {
                System.err.println( e.getLocalizedMessage() );
            }
        }
    }

    public void write(String message) throws IOException
    {
        this.server.serverWrite( "PRIVMSG " + getName() + " :" + message );
    }
}
