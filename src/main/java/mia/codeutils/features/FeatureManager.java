package mia.codeutils.features;

import mia.codeutils.features.impl.development.CPUDisplay;
import mia.codeutils.features.impl.development.CodeSignColorer;
import mia.codeutils.features.impl.development.ItemTagViewer;
import mia.codeutils.features.impl.development.SignPeek;
import mia.codeutils.features.impl.development.chest_previewer.ChestViewer;
import mia.codeutils.features.impl.general.AutoTip;
import mia.codeutils.features.impl.general.chat.SimplifiedStaffChatTags;
import mia.codeutils.features.impl.general.title.JoinButton;
import mia.codeutils.features.impl.internal.ConfigScreenFeature;
import mia.codeutils.features.impl.internal.commands.CommandAliaser;
import mia.codeutils.features.impl.internal.commands.CommandScheduler;
import mia.codeutils.features.impl.internal.mode.LocationAPI;
import mia.codeutils.features.impl.internal.server.ServerManager;
import mia.codeutils.features.impl.internal.staff.VanishTracker;
import mia.codeutils.features.impl.internal.superdupertopsecrte.VerboseLogger;
import mia.codeutils.features.impl.moderation.BetterVanishMSG;
import mia.codeutils.features.impl.moderation.VanishFly;
import mia.codeutils.features.impl.moderation.reports.clickonreportsinchattoteleporttothem;
import mia.codeutils.features.impl.moderation.tracker.HitRange;
import mia.codeutils.features.impl.moderation.tracker.PlayerOutliner;
import mia.codeutils.features.impl.support.AutoQueue;
import mia.codeutils.features.impl.support.hud.SupportHUD;
import mia.codeutils.features.listeners.AbstractEventListener;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@SuppressWarnings({"unchecked"})
public final class FeatureManager {
    private static HashMap<FeatureListener, List<? extends AbstractEventListener>> listeners;
    private static HashMap<Class<? extends Feature>, Feature> features;

    // config has to load b4 features are
    public static void init() {
        listeners = new HashMap<>();
        features = new HashMap<>();
        initFeatures();

        // register listeners
        FeatureListener.getFeatureIdentifiers().forEach(featureListener -> listeners.put(featureListener, getFeaturesByIdentifier(featureListener.getIdentifier())));
    }

    private static void initFeatures() {

        add(new JoinButton(Categories.GENERAL));
        add(new AutoTip(Categories.GENERAL));
        add(new SimplifiedStaffChatTags(Categories.GENERAL));

        add(new CodeSignColorer(Categories.DEV));
        add(new ChestViewer(Categories.DEV));
        add(new SignPeek(Categories.DEV));
        add(new CPUDisplay(Categories.DEV));
        add(new ItemTagViewer(Categories.DEV));

        add(new AutoQueue(Categories.SUPPORT));
        add(new SupportHUD(Categories.SUPPORT));

        add(new BetterVanishMSG(Categories.MODERATION));
        add(new VanishFly(Categories.MODERATION));
        add(new clickonreportsinchattoteleporttothem(Categories.MODERATION));
        add(new HitRange(Categories.MODERATION));
        add(new PlayerOutliner(Categories.MODERATION));

        initInternalFeatures();
    }

    private static void initInternalFeatures() {
        add(new ServerManager(Categories.INTERNAL));
        add(new ConfigScreenFeature(Categories.INTERNAL));
        add(new CommandAliaser(Categories.INTERNAL));
        add(new CommandScheduler(Categories.INTERNAL));
        add(new VanishTracker(Categories.INTERNAL));
        add(new LocationAPI(Categories.INTERNAL));
        add(new VerboseLogger(Categories.INTERNAL));
    }

    private static void add(Feature feature) {
        features.put(feature.getClass(), feature);
    }


    public static <T extends Feature> boolean hasFeature(Class<T> identifier) { return features.containsKey(identifier); }
    public static <T extends Feature> T getFeature(Class<T> identifier) { return (T) features.get(identifier); }
    public static Collection<Feature> getFeatures() { return getFeatureMap().values(); }
    public static HashMap<Class<? extends Feature>, Feature> getFeatureMap() { return features; }

    public static <T extends AbstractEventListener> List<T> getFeaturesByIdentifier(Class<T> listener) {
        return getFeatures().stream().filter((listener::isInstance)).map((feature -> (T) feature)).filter(feature -> ((Feature) feature).getEnabled()).collect(Collectors.toList());
    }

    public static <T extends AbstractEventListener> void implementFeatureListener(Class<T> listener, Consumer<T> consumer) {
        getFeaturesByIdentifier(listener).forEach(consumer);
    }

    //public static List<? extends AbstractEventListener> getFeaturesByListener(FeatureListener featureListener) { return getFeaturesByIdentifier(featureListener.getIdentifier()); }
}

