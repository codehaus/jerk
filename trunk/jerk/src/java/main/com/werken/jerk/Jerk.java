package com.werken.jerk;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import java.util.Properties;
import java.util.Enumeration;
import java.util.Iterator;

public class Jerk
{
    private Map servers;

    private Map commands;
    private Map services;

    public Jerk()
    {
        this.servers = new HashMap();

        this.commands     = new HashMap();
        this.services = new HashMap();
    }

    public void addCommand(String name,
                           Command command)
    {
        this.commands.put( name,
                           command );
    }

    public Command getCommand(String name)
    {
        return (Command) this.commands.get( name );
    }

    public void addService(String name,
                           Service service)
    {
        this.services.put( name,
                           service );
    }

    public Service getService(String name)
    {
        return (Service) this.services.get( name );
    }

    public void startServices(Channel channel) throws IOException, JerkException
    {
        Iterator serviceNameIter = this.services.keySet().iterator();
        String   eachServiceName = null;

        while ( serviceNameIter.hasNext() )
        {
            eachServiceName = (String) serviceNameIter.next();

            channel.write( "starting " + eachServiceName + " service" );
            channel.startService( eachServiceName );
            channel.write( "started " + eachServiceName + " service" );
        }
    }

    public ChannelService startChannelService(Channel channel,
                                              String name) throws JerkException
    {
        Service service = getService( name );

        if ( service == null )
        {
            throw new JerkException( "no known service " + name );
        }

        return service.startChannelService( channel );
    }

    public void stopChannelService(Channel channel,
                                   String name) throws JerkException
    {
        Service service = getService( name );

        if ( service == null )
        {
            throw new JerkException( "no known service " + name );
        }

        service.stopChannelService( channel );
    }

    public void connect(String address,
                        String nick)
    {
        connect( address,
                 6667,
                 nick );
    }

    public void connect(String address,
                        int port,
                        String nick)
    {
        if ( this.servers.containsKey( address ) )
        {
            return;
        }

        Server server = new Server( this,
                                    address,
                                    port,
                                    nick );

        try
        {
            server.connect();
            
            
            this.servers.put( server.getAddress() + ":" + server.getPort(),
                              server );
        }
        catch (IOException e)
        {
            System.err.println( "Unable to connect to " + address + ":" + port );
            return;
        }
    }

    protected static void configure(File configDir,
                                    Jerk jerk) throws IOException
    {
        configureCommands( configDir,
                           jerk );

        configureServices( configDir,
                           jerk );

        configureServers( configDir,
                          jerk );
    }

    protected static void configureCommands(File configDir,
                                            Jerk jerk) throws IOException
    {
        File commandsConf = new File( configDir,
                                      "commands.conf" );
        
        Properties props = new Properties();
        
        props.load( new FileInputStream( commandsConf ) );
        
        Enumeration commandNames = props.propertyNames();
        
        String  className   = null;
        String  commandName = null;
        String  paramList   = null;
        Class   cls         = null;
        Command command     = null;

        while ( commandNames.hasMoreElements() )
        {
            commandName = ((String)commandNames.nextElement()).trim();


            try
            {
                className = props.getProperty( commandName ).trim();

                cls = Class.forName( className );

                command = (Command) cls.newInstance();

                jerk.addCommand( commandName,
                                 command );
            }
            catch (ClassNotFoundException e)
            {
                System.err.println( "unable to locate class :: " + e.getLocalizedMessage() );
            }
            catch (InstantiationException e)
            {
                System.err.println( "unable to instantiate class :: " + e.getLocalizedMessage() );
            }
            catch (IllegalAccessException e)
            {
                System.err.println( "unable to access class :: " + e.getLocalizedMessage() );
            }
        }
    }

    protected static Properties getPropertiesSubset(String prefix,
                                                    Properties props)
    {
        Properties subsetProps = new Properties();

        Enumeration propNames = props.propertyNames();
        String      eachName  = null;

        while ( propNames.hasMoreElements() )
        {
            eachName = (String) propNames.nextElement();

            if ( eachName.startsWith( prefix ) )
            {
                subsetProps.setProperty( eachName.substring( prefix.length() ),
                                         props.getProperty( eachName ) );
            }
        }

        return subsetProps;
    }

    protected static void configureServices(File configDir,
                                            Jerk jerk) throws IOException
    {
        File servicesConf = new File( configDir,
                                      "services.conf" );
        
        Properties props = new Properties();
        
        props.load( new FileInputStream( servicesConf ) );
        
        Enumeration services = props.propertyNames();
        
        String serviceName = null;
        String className = null;

        Service service = null;
        
        while ( services.hasMoreElements() )
        {
            serviceName = (String) services.nextElement();

            if ( serviceName.indexOf( "." ) >= 0 )
            {
                continue;
            }

            className = props.getProperty( serviceName );

            try
            {
                Class serviceClass = Class.forName( className );
                service = (Service) serviceClass.newInstance();

                Properties serviceProps = getPropertiesSubset( "service." + serviceName + "." ,
                                                               props );

                Properties chanServiceProps = getPropertiesSubset( "channel." + serviceName + ".",
                                                                   props );

                service.initialize( jerk,
                                    serviceProps,
                                    chanServiceProps );

                jerk.addService( serviceName,
                                 service );

                Command serviceCommand = service.getCommand();

                if ( serviceCommand != null )
                {
                    jerk.addCommand( serviceName,
                                     serviceCommand );
                }
            }
            catch (Exception e)
            {
                System.err.println( "failed to load service " + serviceName + " :: " + e.getLocalizedMessage() );
            }
        }
    }

    protected static void configureServers(File configDir,
                                           Jerk jerk) throws IOException
    {
        File serversConf = new File( configDir,
                                    "servers.conf" );

        Properties props = new Properties();

        props.load( new FileInputStream( serversConf ) );

        Enumeration servers = props.propertyNames();

        String address = null;
        String portStr = null;
        int    port = 0;

        while ( servers.hasMoreElements() )
        {
            address = (String) servers.nextElement();
            portStr = props.getProperty( address );
            port = Integer.parseInt( portStr );

            jerk.connect( address,
                          port,
                          "jerk" );
        }
    }

    public static void main(String[] args)
    {
        if ( args.length != 1 )
        {
            System.err.println( "must specify a configuration directory" );
            System.exit( 1 );
        }

        Jerk jerk = new Jerk();

        File configDir = new File( args[0] );

        try
        {
            configure(configDir,
                      jerk);
        }
        catch (IOException e)
        {
            System.err.println( e );
            System.exit( 1 );
        }
    }
}
