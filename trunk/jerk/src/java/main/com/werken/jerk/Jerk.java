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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import java.util.Properties;
import java.util.Enumeration;
import java.util.Iterator;

/** The main <code>jerk</code> engine.
 *
 *  @see Server
 *  @see Command
 *  @see Service
 * 
 *  @author <a href="mailto:bob@eng.werken.com">bob mcwhirter</a>
 */
public class Jerk
{
    // ------------------------------------------------------------
    //     Instance members
    // ------------------------------------------------------------

    /** Connected servers. */
    private Map servers;

    /** Installed commands. */
    private Map commands;

    /** Installed services. */
    private Map services;

    // ------------------------------------------------------------
    //     Constructors
    // ------------------------------------------------------------

    /** Construct.
     */
    public Jerk()
    {
        this.servers = new HashMap();

        this.commands     = new HashMap();
        this.services = new HashMap();
    }

    // ------------------------------------------------------------
    //     Instance methods
    // ------------------------------------------------------------

    /** Add a global command.
     *
     *  @param name The name of the command.
     *  @param command The command.
     */
    public void addCommand(String name,
                           Command command)
    {
        this.commands.put( name,
                           command );
    }

    /** Retrieve a command by name.
     *
     *  @param name The name.
     *
     *  @return The command or <code>null</code> if no command
     *          is registered with the specified name.
     */
    public Command getCommand(String name)
    {
        return (Command) this.commands.get( name );
    }

    /** Add a service.
     *
     *  @param name The name of the service.
     *  @param service The service.
     */
    public void addService(String name,
                           Service service)
    {
        this.services.put( name,
                           service );
    }

    /** Retrieve a service by name.
     *
     *  @param name The name.
     *
     *  @return The service of <code>null</code> if no service
     *          is registered with the specified name.
     */
    public Service getService(String name)
    {
        return (Service) this.services.get( name );
    }


    /** Start services on a channel
     *
     *  @param channel The channel to start services on.
     *
     *  @throws IOException If an errors occurs while performing IO.
     *  @throws JerkException If an error occrus while attempting to
     *          start a service.
     */
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

    /** Start a single service on a channel
     *
     *  @param channel The channel to start the service on.
     *  @param name The name of the service to start.
     *
     *  @return The newly created channel service.
     *
     *  @throws JerkException If an error occurs while attempting
     *          to start the service.
     */
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

    /** Stop a single service on a channel
     *
     *  @param channel The channel to stop the service on.
     *  @param name The name of the service to stop.
     *
     *  @throws JerkException If an error occurs while attempting
     *          to stop the service.
     */
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

    /** Connect to an IRC server, using the default port of 6667.
     *
     *  @param address The server address.
     *  @param nick The nickname to use.
     *
     *  @throws IOException If an error occurs while attempting
     *          to connect.
     */
    public void connect(String address,
                        String nick) throws IOException
    {
        connect( address,
                 6667,
                 nick );
    }

    /** Connect to an IRC server.
     *
     *  @param address The server address.
     *  @param port The server port.
     *  @param nick The nickname to use.
     *
     *  @throws IOException If an error occurs while attempting
     *          to connect.
     */
    public void connect(String address,
                        int port,
                        String nick) throws IOException
    {
        if ( this.servers.containsKey( address ) )
        {
            return;
        }

        Server server = new Server( this,
                                    address,
                                    port,
                                    nick );

        server.connect();
        
        
        this.servers.put( server.getAddress() + ":" + server.getPort(),
                          server );
    }

    /** Configure the jerk from a configuration directory.
     *
     *  @param configDir The configuration directory.
     *  @param jerk The jerk to configure.
     *
     *  @throws IOException If an error occurs reading configuration
     *          files. 
     */
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

    /** Configure global commands from <code>commands.conf</code>.
     *
     *  @param configDir The configuration directory.
     *  @param jerk The jerk to configure.
     *
     *  @throws IOException If an error occurs reading configuration
     *          files. 
     */
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

    /** Retrieve the sub-set of properties with the given prefix,
     *  with the prefix removed from the name.
     *
     *  @param prefix The prefix.
     *  @param props The source properties.
     *
     *  @return The unprefixed properties matching the prefix.
     */
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

    /** Configure global services from <code>services.conf</code>.
     *
     *  @param configDir The configuration directory.
     *  @param jerk The jerk to configure.
     *
     *  @throws IOException If an error occurs reading configuration
     *          files. 
     */
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

    /** Configure the jerk's IRC server connection from <code>servers.conf</code>.
     *
     *  @param configDir The configuration directory.
     *  @param jerk The jerk to configure.
     *
     *  @throws IOException If an error occurs reading configuration
     *          files. 
     */
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

    // ------------------------------------------------------------
    //     Class methods
    // ------------------------------------------------------------

    /** Main command-line entrypoint to start the jerk.
     *
     *  @param args Command-line arguments.
     */
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
