package mia.modmod.render.screens.modqa;

import mia.modmod.ColorBank;
import mia.modmod.Mod;
import mia.modmod.features.FeatureManager;
import mia.modmod.features.impl.moderation.ModQA;
import mia.modmod.features.impl.moderation.tracker.PlayerTracker;
import mia.modmod.features.impl.moderation.tracker.punishments.*;
import mia.modmod.render.screens.FrameIndependentAnimation;
import mia.modmod.render.screens.AnimationStage;
import mia.modmod.render.util.*;
import mia.modmod.render.util.Point;
import mia.modmod.render.util.elements.*;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ModQAScreen extends Screen {
    private final Screen parent;
    public final FrameIndependentAnimation animation;

    private String selectedPlayer = null;
    private boolean hasMovedCursor = false;
    private Point firstMousePosition = null;

    private ArrayList<DrawButton> buttons = new ArrayList<>();

    public ModQAScreen(Screen parent) {
        super(Component.literal("MODQA"));
        this.parent = parent;
        this.animation = new FrameIndependentAnimation(AnimationStage.OPENING, 0f, EasingFunctions::easeInOutCircular);
        if (!PlayerTracker.getTrackerPlayers().isEmpty()) this.selectedPlayer = PlayerTracker.getTrackerPlayers().getFirst();
    }

    private void setSelectedPlayer(String playerName) {
        this.selectedPlayer = playerName;
    }

    @Override
    protected void init() {

    }

    public void draw(GuiGraphics context, int mouseX, int mouseY) {
        Point screen = new Point(Mod.getScaledWindowWidth(), Mod.getScaledWindowHeight());
        int mainContainerWidth = 500+100;
        int mainContainerHeight = 300;
        this.buttons = new ArrayList<>();

        int mainColor = ColorBank.BLACK;
        int buttonColor = 0x1f1f1f;
        int enabledColor = 0x3d3d3d;

        DrawRect mainContainer = new DrawRect(screen.mul(0.5, 0.5).add((int)(50*(1-animation.getProgress())),0), new Point(mainContainerWidth, mainContainerHeight),  new ARGB(ColorBank.BLACK, 0.3f * animation.getProgress()));
        mainContainer.setSelfBinding(new DrawBinding(AxisBinding.MIDDLE, AxisBinding.MIDDLE));

        DrawRect sidebarContainer = new DrawRect(Point.ZERO, new Point(100, mainContainerHeight), new ARGB(mainColor, 0.4f * animation.getProgress()), mainContainer);
        DrawCustomToolTip customToolTip = null;


        int playerNameMargin = 4;
        int blockSize = Mod.MC.font.lineHeight + playerNameMargin * 2;
        Point playerContainerSize = new Point(sidebarContainer.getWidth(), blockSize);
        int i = 0;
        if (!PlayerTracker.getTrackerPlayers().isEmpty()) {
            for (String playerName : PlayerTracker.getTrackerPlayers()) {
                DrawButton playerContainer = new DrawButton(
                        new Point(0, (playerContainerSize.y() + 1) * i),
                        playerContainerSize,
                        new ARGB(playerName.equals(selectedPlayer) ? enabledColor : buttonColor, 1f * animation.getProgress()),
                        new ARGB(enabledColor, 1f * animation.getProgress()),
                        sidebarContainer
                );
                playerContainer.setRenderWithScissors(true, true);

                playerContainer.setCallback(() -> {
                    setSelectedPlayer(playerName);
                });
                buttons.add(playerContainer);

                DrawRect playerContainerBottom = new DrawRect(
                        new Point(0,0),
                        new Point(playerContainer.getWidth(), 1),
                        new ARGB(ColorBank.WHITE_GRAY, 0.2f * animation.getProgress()),
                        playerContainer
                );
                playerContainerBottom.setParentBinding(new DrawBinding(AxisBinding.NONE, AxisBinding.FULL));
                playerContainerBottom.setSelfBinding(new DrawBinding(AxisBinding.NONE, AxisBinding.FULL));

                DrawText playerNameText = new DrawText(new Point(playerNameMargin, 0), Component.literal(playerName), animation.getProgress(), true, playerContainer);
                playerNameText.setParentBinding(new DrawBinding(AxisBinding.NONE, AxisBinding.MIDDLE));
                playerNameText.setSelfBinding(new DrawBinding(AxisBinding.NONE, AxisBinding.MIDDLE));
                i++;
            }

            DrawRect titleBar = new DrawRect(Point.ZERO, new Point(mainContainer.getWidth() - sidebarContainer.getWidth(), blockSize), new ARGB(mainColor, 0.7f * animation.getProgress()), sidebarContainer);
            titleBar.setParentBinding(new DrawBinding(AxisBinding.FULL, AxisBinding.NONE));

            DrawText titleBarText = new DrawText(new Point(playerNameMargin, 0), Component.literal(selectedPlayer).withColor(ColorBank.WHITE), animation.getProgress(), false, titleBar);
            titleBarText.setParentBinding(new DrawBinding(AxisBinding.NONE, AxisBinding.MIDDLE));
            titleBarText.setSelfBinding(new DrawBinding(AxisBinding.NONE, AxisBinding.MIDDLE));

            DrawText joinText = new DrawText(new Point(0, 0), Component.literal(" Joined: " + PlayerTracker.getPlayerJoinDateString(selectedPlayer)).withColor(ColorBank.WHITE_GRAY), animation.getProgress(), false, titleBarText);
            joinText.setParentBinding(new DrawBinding(AxisBinding.FULL, AxisBinding.NONE));

            DrawText shiftText = new DrawText(new Point(-playerNameMargin, 0), Component.literal("shift to silent").withColor(ColorBank.MC_GRAY).withStyle(ChatFormatting.ITALIC), animation.getProgress(), true, titleBar);
            shiftText.setParentBinding(new DrawBinding(AxisBinding.FULL, AxisBinding.MIDDLE));
            shiftText.setSelfBinding(new DrawBinding(AxisBinding.FULL, AxisBinding.MIDDLE));

            int dividerSize = 2;
            int punishmentWidth = ((mainContainer.getWidth() - sidebarContainer.getWidth()) - dividerSize * 3) / 2;
            int punishmentHeight = (mainContainer.getHeight() - titleBar.getHeight()) - (dividerSize * 2);

            // ban container

            DrawRect banContainer = new DrawRect(new Point(dividerSize, dividerSize), new Point(punishmentWidth, punishmentHeight), new ARGB(ColorBank.BLACK, 0.3f * animation.getProgress()), titleBar);
            banContainer.setParentBinding(new DrawBinding(AxisBinding.NONE, AxisBinding.FULL));

            // mute container

            DrawRect muteContainer = new DrawRect(new Point(dividerSize, 0), new Point(punishmentWidth, punishmentHeight), new ARGB(ColorBank.BLACK, 0.3f * animation.getProgress()), banContainer);
            muteContainer.setParentBinding(new DrawBinding(AxisBinding.FULL, AxisBinding.NONE));

            // titles

            DrawRect banContainerTitle = new DrawRect(Point.ZERO, new Point(punishmentWidth, blockSize), new ARGB(ColorBank.BLACK, 0.9f * animation.getProgress()), banContainer);
            DrawRect muteContainerTitle = new DrawRect(Point.ZERO, new Point(punishmentWidth, blockSize), new ARGB(ColorBank.BLACK, 0.9f * animation.getProgress()), muteContainer);


            DrawText banContainerTitleText = new DrawText(new Point(playerNameMargin, 0), Component.literal("Ban Options"), animation.getProgress(), true, banContainerTitle);
            banContainerTitleText.setParentBinding(new DrawBinding(AxisBinding.NONE, AxisBinding.MIDDLE));
            banContainerTitleText.setSelfBinding(new DrawBinding(AxisBinding.NONE, AxisBinding.MIDDLE));


            DrawText muteContainerTitleText = new DrawText(new Point(playerNameMargin, 0), Component.literal("Warn / Mute Options"), animation.getProgress(), true, muteContainerTitle);
            muteContainerTitleText.setParentBinding(new DrawBinding(AxisBinding.NONE, AxisBinding.MIDDLE));
            muteContainerTitleText.setSelfBinding(new DrawBinding(AxisBinding.NONE, AxisBinding.MIDDLE));


            // punishment stuff :33


            record OptionButtonList(PunishmentTrack[] optionList, DrawObject parent) {}

            PunishmentTrack[] banOptions = {
                    PunishmentTrack.HACKED_CLIENT,
                    PunishmentTrack.BAN_EVASION,
                    PunishmentTrack.INAPPROPRIATE_SKIN_USERNAME,
                    PunishmentTrack.MACROING,
                    PunishmentTrack.CLIENT_EXPLOITING,
                    PunishmentTrack.INFORMATION_MODS,
                    PunishmentTrack.MALICIOUS_ITEMS,
                    PunishmentTrack.SERVER_CRASHING,
            };

            PunishmentTrack[] muteOptions = {
                    PunishmentTrack.FILTER_BYPASS,
                    PunishmentTrack.SPAMMING,
                    PunishmentTrack.PLOT_AD,
                    PunishmentTrack.DISCRIMINATION,
                    PunishmentTrack.TOXICITY_GENERAL_RUDENESS,
                    PunishmentTrack.TOXICITY_HARASSMENT,
                    PunishmentTrack.TOXICITY_SUICIDE,
                    PunishmentTrack.BANNED_TOPICS,
                    PunishmentTrack.INAPPROPRIATE_CHAT,
                    PunishmentTrack.SEVERELY_INAPPROPRIATE_CHAT,
            };

            PunishmentTracks punishmentHistory = new PunishmentTracks();
            if (PlayerTracker.getTrackedPlayerPunishmentTracks(selectedPlayer).isPresent()) punishmentHistory = PlayerTracker.getTrackedPlayerPunishmentTracks(selectedPlayer).get();

            ArrayList<Component> historyComponents = FeatureManager.getFeature(PlayerTracker.class).getTrackedHistoryText(selectedPlayer);


            //int longestHistoryComponentWidth = 0;
            //for (Component component : historyComponents) if (Mod.MC.font.width(component.getString()) > longestHistoryComponentWidth) longestHistoryComponentWidth = Mod.MC.font.width(component.getString());

            DrawRect historyContainer = new DrawRect(
                    new Point(0, -playerNameMargin),
                    new Point(mainContainer.getWidth(), ((font.lineHeight+1) * historyComponents.size()) + (playerNameMargin * 2)),
                    new ARGB(mainColor, 0.4f * animation.getProgress()),
                    mainContainer
            );
            historyContainer.setSelfBinding(new DrawBinding(AxisBinding.NONE, AxisBinding.FULL));

            int k = 0;
            for (Component component : historyComponents) {
                DrawText historyLine = new DrawText(
                        new Point(playerNameMargin, playerNameMargin + ((font.lineHeight+1) * k)),
                        component,
                        animation.getProgress(),
                        true,
                        historyContainer
                );
                k++;
            }

            k = 0;
            for (Component component : FeatureManager.getFeature(PlayerTracker.class).getTrackedHistoryLastestPunishmentText(selectedPlayer)) {
                DrawText lastestPunishment = new DrawText(
                        new Point(-playerNameMargin, playerNameMargin + ((font.lineHeight+1) * k)),
                        component,
                        animation.getProgress(),
                        true,
                        historyContainer
                );
                lastestPunishment.setParentBinding(new DrawBinding(AxisBinding.FULL, AxisBinding.NONE));
                lastestPunishment.setSelfBinding(new DrawBinding(AxisBinding.FULL, AxisBinding.NONE));
                k++;
            }



            for (OptionButtonList optionButtonList : new OptionButtonList[]{ new OptionButtonList(banOptions, banContainerTitle) , new OptionButtonList(muteOptions, muteContainerTitle) }) {
                int j = 0;
                for (PunishmentTrack punishmentTrack : optionButtonList.optionList()) {
                    PunishmentEscalation punishmentEscalation = punishmentTrack.getPunishmentEscalation();

                    int chances = punishmentEscalation.chances();
                    PunishmentDuration severity = punishmentEscalation.severity();
                    PunishmentDuration maxDuration = punishmentEscalation.maxDuration();
                    ServerPunishmentType punishmentType = punishmentEscalation.punishmentType();

                    ArrayList<PunishmentData> trackHistory = punishmentHistory.getTrackedPunishments().get(punishmentTrack);

                    int numOffenses = 0;

                    if (PunishmentTrack.expiringPunishments.contains(punishmentTrack)) {
                        for (PunishmentData punishmentData : trackHistory) {
                            if (punishmentData.chronoTimestamp().getTimestamp() > ChronoTimestamp.PAST_from_DHMS(32, 0, 0, 0).getTimestamp()) {
                                numOffenses++;
                            }
                        }
                    } else numOffenses = trackHistory.size();


                    int punishLevel = Math.max(((numOffenses - chances) + 1), 0);
                    PunishmentDuration punishmentDuration;

                    if (punishLevel == 0) {
                        punishmentType = ServerPunishmentType.WARN;
                        punishmentDuration = PunishmentDuration.WARNING;
                    } else {
                        punishmentDuration = PunishmentDuration.values()[Math.min((severity.ordinal() + punishLevel) - 1, maxDuration.ordinal())];
                    }

                    ArrayList<Component> optionInfo = new ArrayList<>();

                    Component optionName = punishmentType.getPrefixText().copy().append(Component.literal(" " + punishmentTrack.getReasonText()).withColor(ColorBank.WHITE_GRAY));
                    optionInfo.add(Component.literal(punishmentTrack.getReasonText()));
                    if (!punishmentDuration.getDurationString().isEmpty()) {
                        optionInfo.add(Component.literal("Duration: " + punishmentDuration.getDurationString()).withColor(ColorBank.MC_GRAY));
                    }
                    String command = PunishmentEscalation.buildCommand(selectedPlayer, punishmentType, punishmentDuration.getDurationString(), punishmentTrack.getReasonText(), Mod.MC.hasShiftDown());
                    optionInfo.add(Component.literal(command).withColor(ColorBank.WHITE_GRAY));

                    DrawButton optionButton = new DrawButton(
                            new Point(0, (blockSize + 1) * j + 1),
                            new Point(optionButtonList.parent().getWidth(), blockSize),
                            new ARGB(mainColor, 0.55f * animation.getProgress()),
                            new ARGB(enabledColor, 0.55f * animation.getProgress()),
                            optionButtonList.parent()
                    );
                    optionButton.setRenderWithScissors(true, true);

                    buttons.add(optionButton);
                    optionButton.setParentBinding(new DrawBinding(AxisBinding.NONE, AxisBinding.FULL));

                    if (optionButton.containsPoint(mouseX, mouseY)) {
                        customToolTip = new DrawCustomToolTip(new Point(mouseX, mouseY), optionInfo, 0);

                        ArrayList<PunishmentDuration> tierList = new ArrayList<>(List.of());
                        if (chances > 0) tierList.add(PunishmentDuration.WARNING);
                        tierList.addAll(Arrays.asList(PunishmentDuration.values()).subList(severity.ordinal(), maxDuration.ordinal()+1));

                        int tierWidth = ((int) ((double) mainContainer.getWidth() / tierList.size())) - 1;
                        int tierHeight = 25;


                        DrawRect trackContainer = new DrawRect(
                                new Point(0, playerNameMargin),
                                new Point((tierWidth * tierList.size()) + (tierList.size()-1), tierHeight + ((font.lineHeight+1) * 2) + (playerNameMargin * 3)),
                                new ARGB(mainColor, 0.4f * animation.getProgress()),
                                mainContainer
                        );
                        trackContainer.setParentBinding(new DrawBinding(AxisBinding.MIDDLE, AxisBinding.FULL));
                        trackContainer.setSelfBinding(new DrawBinding(AxisBinding.MIDDLE, AxisBinding.NONE));


                        DrawText trackContainerTitle = new DrawText(
                                new Point(playerNameMargin, playerNameMargin),
                                Component.literal("Punishment Severity:").withColor(ColorBank.WHITE_GRAY),
                                animation.getProgress(),
                                true,
                                trackContainer
                        );


                        int m = 0;
                        for (PunishmentDuration tier : tierList) {
                            Component tierText = Component.literal(tier.getDurationString());
                            boolean isWarning = tier.equals(PunishmentDuration.WARNING);
                            boolean isPerm = tier.equals(PunishmentDuration.PERM);

                            if (isWarning) tierText = Component.literal("Chances: " + Math.max(chances - numOffenses , 0) +" / " + chances);
                            if (isPerm)    tierText = Component.literal("Permanent");

                            int tierRectColor = ARGB.lerpColor(0xffb09e, 0xff461c, ((float) (m+1) / tierList.size()));
                            if (isWarning) tierRectColor = 0xffc94d;
                            if (isPerm) tierRectColor = 0xdb4321;
                            DrawRect eachTierRect = new DrawRect(
                                    new Point(((tierWidth + 1) * m), font.lineHeight + (playerNameMargin * 2)),
                                    new Point( tierWidth, tierHeight),
                                    new ARGB(tierRectColor, 0.75f * animation.getProgress()),
                                    trackContainer
                            );

                            DrawText tierDrawText = new DrawText(
                                    new Point(0, isWarning ? -Mod.MC.font.lineHeight/2 : 0),
                                    isWarning ? Component.literal("Warning") :tierText,
                                    animation.getProgress(),
                                    true,
                                    eachTierRect
                            );
                            tierDrawText.setParentBinding(new DrawBinding(AxisBinding.MIDDLE, AxisBinding.MIDDLE));
                            tierDrawText.setSelfBinding(new DrawBinding(AxisBinding.MIDDLE, AxisBinding.MIDDLE));

                            if (isWarning) {
                                DrawText chanceText = new DrawText(
                                        new Point(0, 1),
                                        tierText,
                                        animation.getProgress(),
                                        true,
                                        tierDrawText
                                );
                                chanceText.setParentBinding(new DrawBinding(AxisBinding.MIDDLE, AxisBinding.FULL));
                                chanceText.setSelfBinding(new DrawBinding(AxisBinding.MIDDLE, AxisBinding.NONE));
                            }

                            if (tier.equals(punishmentDuration)) {
                                DrawOutlineRect outlineRect = new DrawOutlineRect(
                                        Point.ZERO,
                                        eachTierRect.getSize(),
                                        new ARGB(ColorBank.WHITE, 0.7f * animation.getProgress()),
                                        eachTierRect

                                );
                                DrawText indicator = new DrawText(
                                        new Point(0,playerNameMargin),
                                        Component.literal("[Current]").withColor(ColorBank.MC_RED),
                                        animation.getProgress(),
                                        true,
                                        eachTierRect
                                );
                                indicator.setParentBinding(new DrawBinding(AxisBinding.MIDDLE, AxisBinding.FULL));
                                indicator.setSelfBinding(new DrawBinding(AxisBinding.MIDDLE, AxisBinding.NONE));
                            }
                            m++;
                        }
                    }

                    optionButton.setCallback(() -> {
                        if (selectedPlayer.equals(Mod.getPlayerName()) && FeatureManager.getFeature(ModQA.class).safetyMode.getValue()) {
                            Mod.messageError("hey stop that (Safety Mode Active)");
                            return;
                        }

                        // bypass the command scheduler :3
                        // this might cause some issues later but who cares
                        Mod.sendCommand(command);

                        PlayerTracker.getTrackerPlayers().remove(selectedPlayer);
                        if (!PlayerTracker.getTrackerPlayers().isEmpty()) {
                            setSelectedPlayer(PlayerTracker.getTrackerPlayers().getFirst());
                        } else {
                            setSelectedPlayer(null);
                        }

                        optionButton.setCallback(() -> {});
                    });
                    DrawText optionText = new DrawText(
                            new Point(playerNameMargin, 0),
                            optionName,
                            animation.getProgress(),
                            true,
                            optionButton
                    );
                    optionText.setParentBinding(new DrawBinding(AxisBinding.NONE, AxisBinding.MIDDLE));
                    optionText.setSelfBinding(new DrawBinding(AxisBinding.NONE, AxisBinding.MIDDLE));
                    j++;
                }
            }


        } else {
            DrawRect mainErrorContainer = new DrawRect(Point.ZERO, new Point(mainContainer.getWidth() - sidebarContainer.getWidth(), mainContainer.getHeight()), new ARGB(ColorBank.BLACK, 0f), sidebarContainer);
            mainErrorContainer.setParentBinding(new DrawBinding(AxisBinding.FULL, AxisBinding.NONE));


            DrawText errorText = new DrawText(Point.ZERO, Component.literal("No tracked players... /track <player>").withColor(ColorBank.MC_RED), animation.getProgress(), true, mainErrorContainer);
            errorText.setParentBinding(new DrawBinding(AxisBinding.MIDDLE, AxisBinding.MIDDLE));
            errorText.setSelfBinding(new DrawBinding(AxisBinding.MIDDLE, AxisBinding.MIDDLE));
        }

        mainContainer.render(context, mouseX, mouseY);
        if (customToolTip != null) customToolTip.render(context, mouseX, mouseY);
        updateAnimation();
    }

    private void updateAnimation() {
        animation.updateAnimation(0.05f);
    }

    @Override
    public void render(@NonNull GuiGraphics context, int mouseX, int mouseY, float delta) {
        if (parent != null) parent.render(context, Integer.MIN_VALUE, Integer.MIN_VALUE, delta);
        //this.renderBlurredBackground(context);
        if (firstMousePosition == null) {
            firstMousePosition = new Point(mouseX, mouseY);
        } else {
            if (firstMousePosition.equals(new Point(mouseX, mouseY))) hasMovedCursor = true;
        }

        draw(context, mouseX, mouseY);

        super.render(context, mouseX, mouseY, delta);
    }


    @Override
    public void renderBackground(@NonNull GuiGraphics context, int mouseX, int mouseY, float delta) { }


    @Override
    public boolean mouseClicked(@NonNull MouseButtonEvent click, boolean doubled) {
        // to prevent accidentally clicking the first button option when you open screen
        if (!hasMovedCursor) return false;
        for (DrawButton button : buttons) {
            button.mouseClick(click, doubled);
        }
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseReleased(@NonNull MouseButtonEvent click) {
        return super.mouseReleased(click);
    }

    @Override
    public boolean mouseDragged(@NonNull MouseButtonEvent click, double offsetX, double offsetY) {
        return super.mouseDragged(click, offsetX, offsetY);
    }

    @Override
    public boolean charTyped(@NonNull CharacterEvent input) {
        return super.charTyped(input);
    }


    @Override
    public boolean keyPressed(@NonNull KeyEvent input) {
        return super.keyPressed(input);
    }

    @Override
    public void onClose() {
        if (animation.getAnimationStage().equals(AnimationStage.OPEN)) {
            animation.setAnimationStage(AnimationStage.CLOSING);
        }
        if (parent == null) Mod.MC.setScreen(null);
    }
}
