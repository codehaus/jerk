package com.werken.jerk;

public interface ChannelService
{
    void initialize() throws JerkException;
    void shutdown();

    void acceptMessage(Message message) throws JerkException;
}
