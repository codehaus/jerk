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
import java.io.PrintWriter;
import java.io.StringWriter;

import java.util.Map;
import java.util.HashMap;
import java.util.Properties;
import java.util.Enumeration;
import java.util.Iterator;

import java.net.URL;
import java.net.MalformedURLException;
import java.net.URLDecoder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.werken.classworlds.ClassWorld;

import org.apache.commons.cli.Options;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.HelpFormatter;

import com.werken.jerk.config.Configuration;
import com.werken.jerk.config.ConfigurationException;
import com.werken.jerk.config.ConfigurationReader;
import com.werken.jerk.config.Configurator;

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
    public static final String JERK_HOME = "jerk.home";
    
    private static final Log log = LogFactory.getLog(Jerk.class);
    
    // ------------------------------------------------------------
    //     Instance members
    // ------------------------------------------------------------

    /** Connected servers. */
    private Map servers;

    /** Installed commands. */
    private Map commands;

    /** Installed services. */
    private Map services;
    
    private ClassWorld world;
    
    // ------------------------------------------------------------
    //     Constructors
    // ------------------------------------------------------------

    /** Construct.
     */
    public Jerk(final ClassWorld world)
    {
        if (world == null) {
            throw new IllegalArgumentException("world is null");
        }
        
        this.servers = new HashMap();
        this.commands = new HashMap();
        this.services = new HashMap();
        
        this.world = world;
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
        
        if (log.isDebugEnabled())
        {
            log.debug("Connecting to: " + address + ":" + port + ", using nick: " + nick);
        }
        server.connect();
        
        
        this.servers.put( server.getAddress() + ":" + server.getPort(),
                          server );
    }

    private URL configURL;
    
    private String[] processCommandLine(final String[] args) throws Exception
    {
        // create the Options
        Options options = new Options();
        options.addOption(OptionBuilder.withLongOpt("help")
                                       .withDescription("Display this help message")
                                       .create('h'));
                                       
        options.addOption(OptionBuilder.withLongOpt("define")
                                       .withDescription("Define a system property")
                                       .hasArg()
                                       .create('D'));
                                       
        options.addOption(OptionBuilder.withLongOpt("file")
                                       .withDescription("Use a specific configuration file")
                                       .hasArg()
                                       .create('f'));
        
        // create the command line parser
        CommandLineParser parser = new PosixParser();
        
        // parse the command line arguments
        CommandLine line = parser.parse(options, args, true);
        
        // Display command-line help and exit
        if (line.hasOption('h')) {
            System.out.println(Jerk.getBanner());
            System.out.println();
            
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("jerk [options]", "\nOptions:", options, "");
            System.out.println();
            
            System.exit(0);
        }
        
        // Set system properties
        if (line.hasOption('D')) {
            String[] values = line.getOptionValues('D');
            
            for (int i=0; i<values.length; i++) {
                String name, value;
                int j = values[i].indexOf("=");
                
                if (j == -1) {
                    name = values[i];
                    value = "true";
                }
                else {
                    name = values[i].substring(0, j);
                    value = values[i].substring(j + 1, values[i].length());
                }
                
                System.setProperty(name.trim(), value);
            }
        }
        
        if (line.hasOption('f')) {
            String value = line.getOptionValue('f');
            try {
                configURL = new URL(value);
            }
            catch (MalformedURLException e) {
                File file = new File(value);
                
                // For 1.4 use toURI to remove spaces in filename
                // configURL = file.toURI().toURL();
                
                //
                // A preprocessor would be nice right now...
                //
                
                configURL = file.toURL();
            }
        }
        
        return line.getArgs();
    }
    
    public void boot(String[] args) throws Exception
    {
        if (args == null) {
            throw new IllegalArgumentException("args is null");
        }
        
        // Process command-line options
        args = processCommandLine(args);
        
        if (configURL == null) {
            URL homeURL= Jerk.getHomeURL();
            configURL = new URL( homeURL, "conf/jerk.conf" );
        }
        
        ConfigurationReader reader = new ConfigurationReader();
        Configuration config = reader.read(configURL);
        
        configure(config);
    }
    
    public void configure(final Configuration config) throws JerkException
    {
        log.debug("Configuring...");
        
        Configurator c = new Configurator(this);
        c.configure(config);
        
        log.debug("Configured");
    }
    
    // ------------------------------------------------------------
    //     Class methods
    // ------------------------------------------------------------
    
    public static void main(final String[] args, final ClassWorld world)
    {
        try {
            Jerk jerk = new Jerk(world);
            jerk.boot(args);
        }
        catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }
    
    public static void main(final String[] args) throws Exception
    {
        ClassWorld world = new ClassWorld();
        main(args, world);
    }
    
    /////////////////////////////////////////////////////////////////////////
    //                                 Misc                                //
    /////////////////////////////////////////////////////////////////////////
    
    /**
     * Get the fancy <em>Jerk</em> banner text.
     *
     * @return The fancy <em>Jerk</em> banner text.
     */
    public static String getBanner()
    {
        StringWriter writer = new StringWriter();
        PrintWriter out = new PrintWriter(writer);
        
        out.println("     _           _");
        out.println("    | | ___ _ __| | __");
        out.println(" _  | |/ _ \\ '__| |/ /");
        out.println("| |_| |  __/ |  |   <");
        out.print(" \\___/ \\___|_|  |_|\\_\\");
        out.flush();
        
        return writer.toString();
    }
    
    /**
     * Get the <em>Jerk</em> home directory
     *
     * @return The <em>Jerk</em> home directory
     *
     * @throws RuntimeException     Unable to determine home dir.
     */
    public static File getHomeDir()
    {
        // Determine what our home directory is
        String temp = System.getProperty(JERK_HOME);
        File dir = null;
        
        try {
            if (temp == null) {
                String path = Jerk.class.getProtectionDomain().getCodeSource().getLocation().getFile();
                
                // For JDK 1.4...
                // path = URLDecoder.decode(path, "UTF-8");
                path = URLDecoder.decode(path);
                
                // home dir is expected to be lib/..
                dir = new File(path).getParentFile().getParentFile();
            }
            else {
                dir = new File(temp);
            }
            
            // Make sure the home dir does not have any ../ bits
            dir = dir.getCanonicalFile();
        }
        catch (IOException e) {
            throw new JerkRuntimeException("Unable to determine home dir", e);
        }
        
        return dir;
    }
    
    /**
     * Get the <em>Jerk</em> home URL
     *
     * @return The <em>Jerk</em> home URL
     *
     * @throws RuntimeException     Unable to determine home URL.
     */
    public static URL getHomeURL()
    {
        try {
            return getHomeDir().toURL();
        }
        catch (MalformedURLException e) {
            throw new JerkRuntimeException("Unable to determine home URL", e);
        }
    }
}
