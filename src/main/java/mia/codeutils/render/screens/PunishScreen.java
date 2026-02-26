package mia.codeutils.render.screens;

import mia.codeutils.ColorBank;
import mia.codeutils.Mod;
import mia.codeutils.features.impl.moderation.PunishFeature;
import mia.codeutils.render.util.ARGB;
import mia.codeutils.render.util.AxisBinding;
import mia.codeutils.render.util.DrawBinding;
import mia.codeutils.render.util.Point;
import mia.codeutils.render.util.elements.*;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;

public class PunishScreen extends Screen {
    private final Screen parent;
    private final String username;

    private static ArrayList<String> recentReasonsStrings = new ArrayList<>();

    // gay

    private abstract class Punishment {
        protected String type, duration;
        protected Punishment(String type, String duration) {
            this.type = type;
            this.duration = duration;
        }
    }

    private class Warn extends Punishment {
        public Warn() { super("warn", ""); }
    }

    private class Mute extends Punishment {
        public Mute(String duration) { super("mute", duration); }
    }

    private class Ban extends Punishment {
        public Ban(String duration) { super("ban", duration); }
    }

    private class DrawLabeledPunishmentOption extends DrawLabeledButton {
        private final DrawToggleOutlineRect drawToggleOutlineRect;
        private final Punishment punishment;

        public DrawLabeledPunishmentOption(Point position, Point size, Component text, Punishment punishment, int z, boolean shadow, ARGB disabledColor, ARGB enabledColor, ARGB outlineColor, DrawObject parent) {
            super(position, size, text, z, shadow, disabledColor, enabledColor, parent);
            this.punishment = punishment;
            this.drawToggleOutlineRect = new DrawToggleOutlineRect(new Point(0,0), this.getSize(), 0, outlineColor, this);
            addDrawable(drawToggleOutlineRect);
        }

        @Override
        public void setEnabled(boolean enabled) {
            super.setEnabled(enabled);
            drawToggleOutlineRect.setEnabled(enabled);

        }

        @Override
        public void leftMouseClick(MouseButtonEvent click, boolean doubled) {
            super.leftMouseClick(click, doubled);
            setSelectedPunishment(punishment);
        };

        public Punishment getPunishment() { return punishment; }
    }

    // buttons

    private DrawRect mainContainer, titleBar, recentSidebar, selectorContainer;
    private DrawRect muteContainer, banContainer;
    private DrawText titleText;

    private ArrayList<DrawLabeledPunishmentOption> muteButtons, banButtons;
    private DrawLabeledButton warnButton, silentPunishment, nonSilentPunishment;

    private ArrayList<DrawButton> buttons = new ArrayList<>();

    private Punishment selectedPunishment;
    private MultiLineEditBox reasonInputWidget;

    private void setSelectedPunishment(Punishment punishment) {
        selectedPunishment = punishment;
        updateTitleText(punishment);
    }

    private String getCurrentPunishmentCommand(Punishment punishment, boolean silent) {
        return "/" + punishment.type + " " + username + " " + punishment.duration + (punishment.duration.isEmpty() ? "" : " ") + (silent ? "-s " : "") + reasonInputWidget.getValue();
    }

    private void updateTitleText(Punishment punishment) {
        String text = "/punish " + username + " - NO PUNISHMENT SELECTED";
        if (punishment != null) {
            text = getCurrentPunishmentCommand(punishment, false);
        }
        titleText.setText(Component.literal(text).withColor(0xfa4848));
    }
    private void registerButton(DrawButton button) { buttons.add(button); }



    public PunishScreen(Screen parent, String username) {
        super(Component.literal("Punish Screen"));
        this.parent = parent;
        this.username = username;
    }

    @Override
    protected void init() {
        buttons = new ArrayList<>();
        initContainers();
    }

    private void initContainers() {
        Point screenSize = new Point(Mod.getScaledWindowWidth(), Mod.getScaledWindowHeight());
        int mainContainerWidth = 600;
        int mainContainerHeight = 400;
        mainContainer = new DrawRect(screenSize.mul(0.5, 0.5), new Point(mainContainerWidth, mainContainerHeight), 0, new ARGB(ColorBank.BLACK, 0.0f));
        mainContainer.setSelfBinding(new DrawBinding(AxisBinding.MIDDLE, AxisBinding.MIDDLE));

        initMainContainerElements();
    }

    private void initMainContainerElements() {
        int reasonInput = 25;
        int selectorMargin = 5;
        int reasonTextMargin = 2;

        titleBar = new DrawRect(new Point(0,0), new Point(mainContainer.getWidth(), 20), 0, new ARGB(ColorBank.BLACK, 0.75f), mainContainer);
        titleText = new DrawText(new Point(7,10), Component.literal("").withStyle(ChatFormatting.ITALIC), 0, true, titleBar);
        updateTitleText(selectedPunishment);
        titleText.setSelfBinding(new DrawBinding(AxisBinding.NONE, AxisBinding.MIDDLE));

        recentSidebar = new DrawRect(new Point(0,0), new Point(150, mainContainer.getHeight()-titleBar.getHeight()), 0, new ARGB(ColorBank.BLACK, 0.2f), titleBar);
        recentSidebar.setParentBinding(new DrawBinding(AxisBinding.NONE, AxisBinding.FULL));

        DrawText recentReasonsText = new DrawText(new Point(selectorMargin, selectorMargin), Component.literal("Recent Reasons:").withStyle(ChatFormatting.GRAY), 0, true, recentSidebar);

        int i = 0;
        for (String reason : recentReasonsStrings) {
            int reasonHeight = Mod.MC.font.lineHeight + 7;
            DrawButton reasonButton = new DrawButton(
                    new Point(0,2 + selectorMargin + recentReasonsText.getHeight() + (reasonHeight + 1) * i),
                    new Point(recentSidebar.getWidth(), reasonHeight),
                    0,
                    new ARGB(ColorBank.BLACK, 0.6f),
                    new ARGB(ColorBank.MC_DARK_GRAY, 0.6f),
                    recentSidebar
            );
            reasonButton.setCallback(() -> {
                Mod.message(reason);
                reasonInputWidget.setValue(reason, true);
            });
            registerButton(reasonButton);
            DrawText reasonButtonText = new DrawText(new Point(5, 0), Component.literal(reason).withColor(ColorBank.WHITE_GRAY), 0, true, reasonButton);
            reasonButtonText.setSelfBinding(new DrawBinding(AxisBinding.NONE, AxisBinding.MIDDLE));
            reasonButtonText.setParentBinding(new DrawBinding(AxisBinding.NONE, AxisBinding.MIDDLE));
            i++;
        }




        selectorContainer = new DrawRect(new Point(0,0), new Point(mainContainer.getWidth()- recentSidebar.getWidth(), mainContainer.getHeight()-titleBar.getHeight()), 0, new ARGB(ColorBank.BLACK, 0.45f), recentSidebar);
        selectorContainer.setParentBinding(new DrawBinding(AxisBinding.FULL, AxisBinding.NONE));


        DrawText reasonText = new DrawText(new Point(selectorMargin, selectorMargin), Component.literal("Reason:").withStyle(ChatFormatting.GRAY), 0, true, selectorContainer);

        reasonInputWidget = MultiLineEditBox.builder()
            .setX(reasonText.x1())
            .setY(reasonText.y2()+reasonTextMargin)
            .build(Mod.MC.font, selectorContainer.getWidth() - (selectorMargin * 2), reasonInput, Component.literal(""));
        this.addRenderableOnly(reasonInputWidget);
        this.setInitialFocus(reasonInputWidget);


        // punishment options

        DrawText warnText = new DrawText(new Point(selectorMargin, selectorMargin * 2 + reasonInput + reasonText.getHeight() + reasonTextMargin), Component.literal("Warn:").withStyle(ChatFormatting.GRAY), 0, true, selectorContainer);

        warnButton = new DrawLabeledPunishmentOption(
                new Point(0,2),
                new Point(50, 275),
                Component.literal("Warn"),
                new Warn(),
                0,
                true,
                new ARGB(0xff8c8c, 0.7f),
                new ARGB(0xff2424, (0.8f)),
                new ARGB(0x1f1f1f, 0.8f),
                warnText
        );
        warnButton.setParentBinding(new DrawBinding(AxisBinding.NONE, AxisBinding.FULL));

        warnButton.setCallback(() -> {
            boolean setEnabled = !warnButton.isEnabled();
            if (setEnabled) {
                for (DrawLabeledPunishmentOption button : muteButtons) button.setEnabled(false);
                for (DrawLabeledPunishmentOption button : banButtons) button.setEnabled(false);
                warnButton.setEnabled(false);
            }
            warnButton.setEnabled(setEnabled);
        });
        registerButton(warnButton);


        DrawText muteText = new DrawText(warnText.getRawPosition().add(new Point(selectorMargin + warnButton.getWidth(), 0)), Component.literal("Mute Durations:").withStyle(ChatFormatting.GRAY), 0, true, selectorContainer);

        int punishmentContainerHeight = (int) Math.ceil((double) (warnButton.getHeight() - (selectorMargin + 2 + muteText.getHeight())) / 2.0);

        muteContainer = new DrawRect(new Point(0, 2), new Point(selectorContainer.getWidth() - (warnButton.getWidth() + selectorMargin * 3), punishmentContainerHeight),0, new ARGB(ColorBank.MC_RED, 0.0f), muteText);
        muteContainer.setParentBinding(new DrawBinding(AxisBinding.NONE, AxisBinding.FULL));

        DrawText banText = new DrawText(new Point(0, selectorMargin), Component.literal("Ban Durations:").withStyle(ChatFormatting.GRAY), 0, true, muteContainer);
        banText.setParentBinding(new DrawBinding(AxisBinding.NONE, AxisBinding.FULL));

        banContainer = new DrawRect(new Point(0, 2), new Point(muteContainer.getWidth(), punishmentContainerHeight),0, new ARGB(ColorBank.MC_RED, 0.0f), banText);
        banContainer.setParentBinding(new DrawBinding(AxisBinding.NONE, AxisBinding.FULL));

        enum BanOption {
            H1("1 Hour", "1h"),
            D1("1 Day", "1d"),
            D3("3 Days", "3d"),
            D7("7 Days", "7d"),
            D14("14 Days", "14d"),
            D30("30 Days", "30d"),
            D90("90 Days", "90d"),

            PERM("Perm", "");

            private final String name, duration;
            BanOption(String name, String duration) {
                this.name = name;
                this.duration = duration;
            }
        }

        enum MuteOption {
            H1("1 Hour", "1h"),
            D1("1 Day", "1d"),
            D3("3 Days", "3d"),
            D7("7 Days", "7d"),
            D14("14 Days", "14d"),
            D30("30 Days", "30d"),
            D90("90 Days", "90d"),

            PERM("Perm", "");

            private final String name, duration;
            MuteOption(String name, String duration) {
                this.name = name;
                this.duration = duration;
            }
        }
        int numValues, eachWidth;

        muteButtons = new ArrayList<>();
        banButtons = new ArrayList<>();

        // need to refactored at some point to get rid of the repeated code

        i = 0;
        numValues = MuteOption.values().length;
        eachWidth = muteContainer.getWidth() / numValues;
        for (MuteOption muteDuration : MuteOption.values()) {
            DrawLabeledPunishmentOption muteOption = new DrawLabeledPunishmentOption(
                    new Point(eachWidth * i, 0),
                    new Point(eachWidth, muteContainer.getHeight()),
                    Component.literal(muteDuration.name),
                    new Mute(muteDuration.duration),
                    0,
                    true,
                    new ARGB(ARGB.lerpColor(0xff8c8c, 0xff3030, (float) (i) / numValues), 0.6f),
                    new ARGB(0xff2424, (0.8f)),
                    new ARGB(0x1f1f1f, 0.8f),
                    muteContainer
            );

            muteOption.setCallback(() -> {
                boolean setEnabled = !muteOption.isEnabled();
                if (setEnabled) {
                    for (DrawLabeledPunishmentOption button : muteButtons) button.setEnabled(false);
                    for (DrawLabeledPunishmentOption button : banButtons) button.setEnabled(false);
                    warnButton.setEnabled(false);
                }
                muteOption.setEnabled(setEnabled);
            });
            muteContainer.addDrawable(muteOption);
            muteButtons.add(muteOption);
            registerButton(muteOption);
            i++;
        }

        i = 0;
        numValues = BanOption.values().length;
        eachWidth = banContainer.getWidth() / numValues;
        for (BanOption banDuration : BanOption.values()) {
            DrawLabeledPunishmentOption banOption = new DrawLabeledPunishmentOption(
                    new Point(eachWidth * i, 0),
                    new Point(eachWidth, banContainer.getHeight()),
                    Component.literal(banDuration.name),
                    new Ban(banDuration.duration),
                    0,
                    true,
                    new ARGB(ARGB.lerpColor(0xff8c8c, 0xff3030, (float) (i) / numValues), 0.6f),
                    new ARGB(0xff2424, (0.8f)),
                    new ARGB(0x1f1f1f, 0.8f),
                    banContainer
            );
            banOption.setCallback(() -> {
                boolean setEnabled = !banOption.isEnabled();
                if (setEnabled) {
                    for (DrawLabeledPunishmentOption button : muteButtons) button.setEnabled(false);
                    for (DrawLabeledPunishmentOption button : banButtons) button.setEnabled(false);
                    warnButton.setEnabled(false);
                }
                banOption.setEnabled(setEnabled);
            });

            banContainer.addDrawable(banOption);
            banButtons.add(banOption);
            registerButton(banOption);
            i++;
        }


        // public execution simulator

        DrawLabeledButton silentPunishment, nonSilentPunishment;
        silentPunishment = new DrawLabeledButton(
                new Point(0, selectorMargin),
                new Point(
                        selectorContainer.getWidth() - (selectorMargin * 2),
                        (selectorContainer.getHeight() - (((warnButton.getPosition().y() + warnButton.getHeight()) - selectorContainer.getPosition().y()) + selectorMargin * 3)) /2
                ),
                Component.literal("Silent Execute"),
                0,
                true,
                new ARGB(0xbfbfbf, 0.8f),
                new ARGB(0xf0f0f0, (0.8f)),
                warnButton
        );
        silentPunishment.setParentBinding(new DrawBinding(AxisBinding.NONE, AxisBinding.FULL));
        silentPunishment.setCallback(() -> {
            if (selectedPunishment != null) {
                Mod.sendCommand(getCurrentPunishmentCommand(selectedPunishment, true));
                if (!reasonInputWidget.getValue().isEmpty()) recentReasonsStrings.add(reasonInputWidget.getValue());
                onClose();
            } else {
                Mod.messageError("No punishment type selected!");
            }
        });
        registerButton(silentPunishment);


        nonSilentPunishment = new DrawLabeledButton(
                new Point(0, selectorMargin),
                silentPunishment.getSize(),
                Component.literal("Execute"),
                0,
                true,
                new ARGB(0x76d463, 0.8f),
                new ARGB(0x79fa5f, (0.8f)),
                silentPunishment
        );
        nonSilentPunishment.setParentBinding(new DrawBinding(AxisBinding.NONE, AxisBinding.FULL));
        nonSilentPunishment.setCallback(() -> {
            if (selectedPunishment != null) {
                Mod.sendCommand(getCurrentPunishmentCommand(selectedPunishment, false));
                if (!reasonInputWidget.getValue().isEmpty()) recentReasonsStrings.add(reasonInputWidget.getValue());
                onClose();
            } else {
                Mod.messageError("No punishment type selected!");
            }
        });
        registerButton(nonSilentPunishment);

    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        if (parent != null) parent.render(context, Integer.MAX_VALUE, Integer.MAX_VALUE, delta);
        //this.applyBlur(context);

        if (mainContainer != null) mainContainer.render(context, mouseX, mouseY);
        updateTitleText(selectedPunishment);

        super.render(context, mouseX, mouseY, delta);
    }
    @Override
    public void renderBackground(GuiGraphics context, int mouseX, int mouseY, float delta) { }


    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        for (DrawButton button : buttons) {
            button.mouseClick(click, doubled);
        }
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent click) {
        return super.mouseReleased(click);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent click, double offsetX, double offsetY) {
        return super.mouseDragged(click, offsetX, offsetY);
    }

    @Override
    public boolean charTyped(CharacterEvent input) {
        return super.charTyped(input);
    }


    @Override
    public boolean keyPressed(KeyEvent input) {
        return super.keyPressed(input);
    }

    @Override
    public void onClose() {
        if (parent == null) Mod.MC.setScreen((Screen) null);
        PunishFeature.resetPunishScreen();
    }


}
