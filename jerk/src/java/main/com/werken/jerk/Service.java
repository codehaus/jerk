package com.werken.jerk;

import java.util.Properties;

public interface Service
{
    void initialize(Jerk jerk,
                    Properties serviceProps,
                    Properties chanServiceProps) throws JerkException;

    void shutdown();

    ChannelService startChannelService(Channel channel) throws JerkException;
    void stopChannelService(Channel channel) throws JerkException;

    Command getCommand();
}
