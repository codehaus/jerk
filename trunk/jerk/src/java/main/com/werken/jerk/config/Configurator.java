package com.werken.jerk.config;

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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.werken.jerk.Jerk;
import com.werken.jerk.JerkException;
import com.werken.jerk.Command;
import com.werken.jerk.Service;
import com.werken.jerk.Server;

import com.werken.jerk.util.StringValueParser;

/**
 * Handles the details of Jerk configuration.
 *
 * @version <code>$Revision$ $Date$</code>
 */
public class Configurator
{
    private static final Log log = LogFactory.getLog(Configurator.class);
    
    protected Jerk jerk;
    protected StringValueParser valueParser;
    
    public Configurator(final Jerk jerk)
    {
        if (jerk == null) {
            throw new IllegalArgumentException("jerk");
        }
        
        this.jerk = jerk;
        this.valueParser = new StringValueParser();
    }
    
    public Jerk getJerk()
    {
        return jerk;
    }
    
    public void configure(final Configuration config) throws JerkException
    {
        if (config == null) {
            throw new IllegalArgumentException("config");
        }
        
        try {
            configureProperties(config.getPropertiesConfig());
            configureCommands(config.getCommandsConfig());
            configureServices(config.getServicesConfig());
            configureServers(config.getServersConfig());
        }
        catch (Exception e) {
            throw new JerkException("Failed to configure", e);
        }
    }
    
    protected void configureProperties(final PropertiesConfig config)
    {
        if (config == null) return;
        
        PropertyConfig[] props = config.getPropertyConfig();
        for (int i=0; i<props.length; i++) {
            if (props[i] == null) {
                throw new IllegalArgumentException("PropertyConfig[" + i + "]");
            }
            
            String name = props[i].getName().trim();
            String value = props[i].getContent();
            value = valueParser.parse(value);
            System.setProperty(name, value);
        }
    }
    
    protected void configureCommands(final CommandsConfig config) throws JerkException
    {
        if (config == null) return;
        
        CommandConfig[] commands = config.getCommandConfig();
        if (commands == null) return;
        
        Jerk jerk = getJerk();
        boolean debug = log.isDebugEnabled();
        
        for (int i=0; i<commands.length; i++) {
            if (commands[i] == null) {
                throw new IllegalArgumentException("CommandConfig[" + i + "]");
            }
            
            String commandName = commands[i].getName();
            if (debug) {
                log.debug("Loading command: " + commandName);
            }
            
            String className = commands[i].getCode();
            if (debug) {
                log.debug("Attempting to load command class: " + className);
            }
            
            try {
                Class type = Class.forName( className );
                
                Command command = (Command) type.newInstance();
                if (debug)
                {
                    log.debug("Created command: " + command);
                }
                
                jerk.addCommand( commandName, command );
            }
            catch (Exception e) {
                throw new ConfigurationException("Failed to configure command: " + commandName, e);
            }
        }
    }
    
    protected Properties loadProperties(final PropertyConfig[] config)
    {
        Properties props = new Properties();
        
        if (config != null) {
            for (int i=0; i<config.length; i++) {
                String value = valueParser.parse(config[i].getContent());
                props.setProperty(config[i].getName(), value);
            }
        }
        
        return props;
    }
    
    protected void configureServices(final ServicesConfig config) throws JerkException
    {
        if (config == null) return;
        
        ServiceConfig[] services = config.getServiceConfig();
        if (services == null) return;
        
        Jerk jerk = getJerk();
        boolean debug = log.isDebugEnabled();
        
        for (int i=0; i<services.length; i++) {
            if (services[i] == null) {
                throw new IllegalArgumentException("ServiceConfig[" + i + "]");
            }
            
            String serviceName = services[i].getName();
            if (debug) {
                log.debug("Loading service: " + serviceName);
            }
            
            String className = services[i].getCode();
            if (debug) {
                log.debug("Attempting to load service class: " + className);
            }
            
            try {
                Class type = Class.forName( className );
                
                Service service = (Service) type.newInstance();
                if (debug)
                {
                    log.debug("Created service: " + service);
                }
                
                Properties serviceProps = null;
                ServicePropertiesConfig spc = services[i].getServicePropertiesConfig();
                if (spc != null) {
                    serviceProps = loadProperties(spc.getPropertyConfig());
                }
                else {
                    serviceProps = new Properties();
                }
                if (debug) {
                    log.debug("Service properties: " + serviceProps);
                }
                
                Properties chanServiceProps = null;
                ServiceChannelPropertiesConfig scpc = services[i].getServiceChannelPropertiesConfig();
                if (scpc != null) {
                    chanServiceProps = loadProperties(scpc.getPropertyConfig());
                }
                else {
                    chanServiceProps = new Properties();
                }
                if (debug) {
                    log.debug("Service channel properties: " + chanServiceProps);
                }
                
                service.initialize( jerk,
                                    serviceProps,
                                    chanServiceProps );
                
                log.debug("Service initialized; adding");
                jerk.addService( serviceName,
                                 service );

                Command serviceCommand = service.getCommand();

                if ( serviceCommand != null )
                {
                    if (debug) {
                        log.debug("Using service command: " + serviceCommand);
                    }
                    
                    jerk.addCommand( serviceName,
                                     serviceCommand );
                }
            }
            catch (Exception e) {
                throw new ConfigurationException("Failed to configure service: " + serviceName, e);
            }
        }
    }
    
    protected void configureServers(final ServersConfig config) throws JerkException
    {
        if (config == null) return;
        
        ServerConfig[] servers = config.getServerConfig();
        if (servers == null) return;
        
        Jerk jerk = getJerk();
        boolean debug = log.isDebugEnabled();
        
        for (int i=0; i<servers.length; i++) {
            if (servers[i] == null) {
                throw new IllegalArgumentException("ServerConfig[" + i + "]");
            }
            
            String hostname = servers[i].getHostname();
            int port = servers[i].getPort();
            if (port < 1) {
                port = 6667;
            }
            String nickname = servers[i].getNickname();
            if (nickname == null) {
                nickname = "jerk";
            }
            
            if (debug) {
                log.debug("Connecting to server: " + hostname + ":" + port +
                          " with nickname: " + nickname);
            }
            
            try {
                jerk.connect(hostname, port, nickname);
            }
            catch (Exception e) {
                throw new ConfigurationException("Failed to connect to: " + hostname + ":" + port, e);
            }
        }
    }
}
