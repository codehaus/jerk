package com.werken.jerk.services.log;

import com.werken.jerk.Channel;

import java.io.File;
import java.io.IOException;
import java.util.Date;

public interface Logger
{
    void logMessage(Date timestamp,
                    String nick,
                    String message) throws IOException;

    void setLog(File logFile) throws IOException;

    void setChannel(Channel channel);
}
