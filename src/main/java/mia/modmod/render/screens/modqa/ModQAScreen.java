package mia.modmod.render.screens.modqa;

import mia.modmod.ColorBank;
import mia.modmod.Mod;
import mia.modmod.features.FeatureManager;
import mia.modmod.features.impl.moderation.tracker.PlayerOutliner;
import mia.modmod.features.impl.moderation.tracker.PlayerTracker;
import mia.modmod.render.screens.Animation;
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

public class ModQAScreen extends Screen {
    private final Screen parent;
    public final Animation animation;

    private String selectedPlayer = null;
    private boolean hasMovedCursor = false;
    private Point firstMousePosition = null;

    private ArrayList<DrawButton> buttons = new ArrayList<>();

    public ModQAScreen(Screen parent) {
        super(Component.literal("MODQA"));
        this.parent = parent;
        this.animation = new Animation(AnimationStage.OPENING, 0f, EasingFunctions::easeInOutCircular);
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
        int mainContainerWidth = 500;
        int mainContainerHeight = 300;
        this.buttons = new ArrayList<>();

        int mainColor = ColorBank.BLACK;
        int buttonColor = 0x1f1f1f;
        int enabledColor = 0x3d3d3d;

        DrawRect mainContainer = new DrawRect(screen.mul(0.5, 0.5).add((int)(50*(1-animation.getProgress())),0), new Point(mainContainerWidth, mainContainerHeight),  new ARGB(ColorBank.BLACK, 0.3f * animation.getProgress()));
        mainContainer.setSelfBinding(new DrawBinding(AxisBinding.MIDDLE, AxisBinding.MIDDLE));

        DrawRect sidebarContainer = new DrawRect(Point.ZERO, new Point(100, mainContainerHeight), new ARGB(mainColor, 0.4f * animation.getProgress()), mainContainer);
        final DrawCustomToolTip[] customToolTip = new DrawCustomToolTip[1];


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
                ) {
                    @Override
                    public void render(GuiGraphics context, int mouseX, int mouseY) {
                        context.enableScissor(this.x1(), this.y1(), this.x2(), this.y2());
                        super.render(context,mouseX,mouseY);
                        context.disableScissor();
                    }
                };
                playerContainer.setCallback(() -> {
                    setSelectedPlayer(playerName);
                });
                buttons.add(playerContainer);
                DrawText playerNameText = new DrawText(new Point(playerNameMargin, 0), Component.literal(playerName), animation.getProgress(), true, playerContainer);
                playerNameText.setParentBinding(new DrawBinding(AxisBinding.NONE, AxisBinding.MIDDLE));
                playerNameText.setSelfBinding(new DrawBinding(AxisBinding.NONE, AxisBinding.MIDDLE));
                i++;
            }

            DrawRect titleBar = new DrawRect(Point.ZERO, new Point(mainContainer.getWidth() - sidebarContainer.getWidth(), blockSize), new ARGB(mainColor, 0.7f * animation.getProgress()), sidebarContainer);
            titleBar.setParentBinding(new DrawBinding(AxisBinding.FULL, AxisBinding.NONE));

            DrawText titleBarText = new DrawText(new Point(playerNameMargin, 0), Component.literal(selectedPlayer).withColor(ColorBank.WHITE_GRAY), animation.getProgress(), true, titleBar);
            titleBarText.setParentBinding(new DrawBinding(AxisBinding.NONE, AxisBinding.MIDDLE));
            titleBarText.setSelfBinding(new DrawBinding(AxisBinding.NONE, AxisBinding.MIDDLE));

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



            Punishment[] banOptions = {
                    new Punishment(PunishmentType.BAN, "30d", "Hacked Client"),
                    new Punishment(PunishmentType.BAN, "perm", "Ban Evasion"),
                    new Punishment(PunishmentType.BAN, "perm", "Inappropriate Skin / Username (Appeal when changed)"),
                    new Punishment(PunishmentType.BAN, "3d", "Macroing / Autoclicking"),
                    new Punishment(PunishmentType.BAN, "3d", "Information Mods"),
                    new Punishment(PunishmentType.BAN, "perm", "Server Exploiting"),
                    new Punishment(PunishmentType.BAN, "3d", "Game Exploiting"),
                    new Punishment(PunishmentType.BAN, "perm", "Alt Account Abuse"),
                    new Punishment(PunishmentType.BAN, "perm", "Mute Evasion")
            };

            Punishment[] muteOptions = {
                    new Punishment(PunishmentType.WARN, "", "Filter Bypass"),
                    new Punishment(PunishmentType.WARN, "", "Spam"),
                    new Punishment(PunishmentType.WARN, "", "Toxicity"),
                    new Punishment(PunishmentType.WARN, "", "Plot Ad Misuse"),
                    new Punishment(PunishmentType.MUTE, "7d", "Suicide Encouragement"),
                    new Punishment(PunishmentType.MUTE, "7d", "Harassment"),
                    new Punishment(PunishmentType.MUTE, "1d", "Banned Topics"),
                    new Punishment(PunishmentType.MUTE, "3d", "Inappropriate Chat"),
                    new Punishment(PunishmentType.MUTE, "14d", "Extremely Inappropriate Chat"),
                    new Punishment(PunishmentType.MUTE, "14d", "Discrimination"),
            };

            record OptionButtonList(Punishment[] optionList, DrawObject parent) {}

            for (OptionButtonList optionButtonList : new OptionButtonList[]{ new OptionButtonList(banOptions, banContainerTitle) , new OptionButtonList(muteOptions, muteContainerTitle) }) {
                int j = 0;
                for (Punishment option : optionButtonList.optionList()) {
                    ArrayList<Component> optionInfo = new ArrayList<>();

                    Component optionName = option.type.prefixText.copy().append(Component.literal(" " + option.reason).withColor(ColorBank.WHITE_GRAY));
                    optionInfo.add(Component.literal(option.reason));
                    if (!option.duration.isEmpty()) {
                        optionInfo.add(Component.literal("Duration: " + option.duration).withColor(ColorBank.MC_GRAY));
                    }
                    String command = option.getCommand(selectedPlayer, Mod.MC.hasShiftDown());
                    optionInfo.add(Component.literal(command).withColor(ColorBank.WHITE_GRAY));

                    DrawButton optionButton = new DrawButton(
                            new Point(0, (blockSize + 1) * j + 1),
                            new Point(optionButtonList.parent().getWidth(), blockSize),
                            new ARGB(mainColor, 0.55f * animation.getProgress()),
                            new ARGB(enabledColor, 0.55f * animation.getProgress()),
                            optionButtonList.parent()
                    ) {
                        @Override
                        public void render(GuiGraphics context, int mouseX, int mouseY) {
                            context.enableScissor(this.x1(), this.y1(), this.x2(), this.y2());
                            super.render(context, mouseX, mouseY);
                            context.disableScissor();
                            if (containsPoint(mouseX, mouseY)) {
                                //DrawContextHelper.drawTooltip(context, optionInfo, mouseX, mouseY, 0);
                                customToolTip[0] = new DrawCustomToolTip(new Point(mouseX, mouseY), optionInfo, 0);
                            }
                        }
                    };
                    optionButton.setCallback(() -> {
                        Mod.sendCommand(command);
                        PlayerTracker.getTrackerPlayers().remove(selectedPlayer);
                        if (!PlayerTracker.getTrackerPlayers().isEmpty()) {
                            setSelectedPlayer(PlayerTracker.getTrackerPlayers().getFirst());
                        } else {
                            setSelectedPlayer(null);
                        }
                    });
                    buttons.add(optionButton);
                    optionButton.setParentBinding(new DrawBinding(AxisBinding.NONE, AxisBinding.FULL));
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
        if (customToolTip[0] != null) customToolTip[0].render(context, mouseX, mouseY);
        updateAnimation();
    }
    private enum PunishmentType {
        WARN("warn", Component.literal("[W]").withColor(0xffd942)),
        MUTE("mute", Component.literal("[M]").withColor(ColorBank.MC_GREEN)),
        BAN("ban", Component.literal("[B]").withColor(ColorBank.MC_RED));

        private final String prefix;
        private final Component prefixText;

        PunishmentType(String prefix, Component prefixText) {
            this.prefix = prefix;
            this.prefixText = prefixText;
        }
    }

    private record Punishment(PunishmentType type, String duration, String reason) {
        public String getCommand(String playerName, boolean silent) {
            return "/" + type.prefix + " " + playerName + (duration.equals("perm") ? "" : (duration.isEmpty() ? "" : " " + duration)) + (silent ? " -s" : "")  + " " + reason;
        }
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
        if (parent == null) Mod.MC.setScreen((Screen) null);
    }
}
