package com.werken.jerk;

import java.io.IOException;

public interface Command
{
    void perform(Message message) throws IOException;
}
