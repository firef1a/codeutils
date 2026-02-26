package mia.codeutils.features;

import mia.codeutils.features.listeners.AbstractEventListener;
import mia.codeutils.features.listeners.impl.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/*
    glorified list of listeners, pls tell me if theres a way to automatically get classes which implement abstracteventlistener w/o reflection
 */
public enum FeatureListener {
    CLIENT_EVENT_LISTENER(ClientEventListener.class),
    COMMAND_REGISTRATION(RegisterCommandListener.class),
    MODE_SWITCH_EVENT_LISTENER(ModeSwitchEventListener.class),
    PACKET_LISTENER(PacketListener.class),
    PLAYER_USE_EVENT_LISTENER(PlayerUseEventListener.class),
    CHAT_EVENT_LISTENER(ChatEventListener.class),
    RENDER_HUD(RenderHUD.class),
    RENDER_TOOLTIP(RenderTooltip.class),
    SERVER_CONNECTION_EVENT_LISTENER(ServerConnectionEventListener.class),
    TICK_EVENT(TickEvent.class),
    REGISTER_KEY_BIND_EVENT(RegisterKeyBindEvent.class);

    private final Class<? extends AbstractEventListener> identifier;

    <T extends AbstractEventListener> FeatureListener(Class<T> identifier) { this.identifier = identifier; }

    public Class<? extends AbstractEventListener> getIdentifier() { return identifier; }

    public static List<FeatureListener> getFeatureIdentifiers() { return List.of(FeatureListener.values()); }
    public static Collection<Class<? extends AbstractEventListener>> getIdentifiers() { return getFeatureIdentifiers().stream().map(FeatureListener::getIdentifier).collect(Collectors.toCollection(ArrayList::new)); }
}

