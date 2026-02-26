package mia.codeutils.features.listeners.impl;

import mia.codeutils.features.listeners.AbstractEventListener;
import mia.codeutils.features.listeners.DFMode;

public interface ModeSwitchEventListener extends AbstractEventListener {
    void onModeSwitch(DFMode newMode, DFMode previousMode);
}
