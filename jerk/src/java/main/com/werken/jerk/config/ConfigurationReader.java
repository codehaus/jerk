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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.exolab.castor.xml.Unmarshaller;

/**
 * Creates <code>Configuration</code> objects.
 *
 * @version <code>$Revision$ $Date$</code>
 */
public class ConfigurationReader
{
    private static final Log log = LogFactory.getLog(ConfigurationReader.class);
    
    /** The Castor unmarshaller used to tranform XML->Objects */
    protected Unmarshaller unmarshaller;
    
    /**
     * Construct a <code>ConfigurationReader</code>.
     */
    public ConfigurationReader()
    {
        unmarshaller = new Unmarshaller(Configuration.class);
    }
    
    /**
     * Read a configuration instance from a URL.
     *
     * @param url   The URL to read the configuration from.
     * @return      The configuration instance.
     *
     * @throws Exception    Failed to read configuration.
     */
    public Configuration read(final URL url) throws Exception
    {
        if (url == null) {
            throw new IllegalArgumentException("url");
        }
        
        if (log.isDebugEnabled()) {
            log.debug("Reading: " + url);
        }
        
        return doRead(new BufferedReader(new InputStreamReader(url.openStream())));
    }
    
    /**
     * Read a configuration instance from a file.
     *
     * @param file  The file to read the configuration from.
     * @return      The configuration instance.
     *
     * @throws Exception    Failed to read configuration.
     */
    public Configuration read(final File file) throws Exception
    {
        if (file == null) {
            throw new IllegalArgumentException("file");
        }
        
        if (log.isDebugEnabled()) {
            log.debug("Reading: " + file);
        }
        
        return doRead(new BufferedReader(new FileReader(file)));
    }
    
    /**
     * Read a configuration instance from a reader.
     *
     * @param reader    The reader to read the configuration from.
     * @return          The configuration instance.
     *
     * @throws Exception    Failed to read configuration.
     */
    public Configuration read(final Reader reader) throws Exception
    {
        if (reader == null) {
            throw new IllegalArgumentException("reader");
        }
        
        return (Configuration)unmarshaller.unmarshal(reader);
    }
    
    /**
     * Read a configuration instance from a reader and handle closing the
     * reader after the read operation.
     *
     * @param reader    The reader to read the configuration from.
     * @return          The configuration instance.
     *
     * @throws Exception    Failed to read configuration.
     */
    protected Configuration doRead(final Reader reader) throws Exception
    {
        Configuration config = null;
        try {
            config = read(reader);
        }
        finally {
            reader.close();
        }
        
        return config;
    }
}
