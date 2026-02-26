package mia.codeutils.features.listeners.impl;

import mia.codeutils.features.listeners.AbstractEventListener;

public interface ClientEventListener extends AbstractEventListener {
    void clientInitialize();
    void clientShutdown();
}
