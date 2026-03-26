package mia.modmod.render.screens.reportscreen;

import mia.modmod.ColorBank;
import mia.modmod.Mod;
import mia.modmod.features.FeatureManager;
import mia.modmod.features.impl.moderation.reports.DatedReport;
import mia.modmod.features.impl.moderation.reports.ReportScreenFeature;
import mia.modmod.features.impl.moderation.tracker.PlayerOutliner;
import mia.modmod.render.screens.Animation;
import mia.modmod.render.screens.AnimationStage;
import mia.modmod.render.util.*;
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
import java.util.List;

public class ReportScreen extends Screen {
    private final PlayerOutliner playerOutliner;
    private final Screen parent;
    public final Animation animation;

    private String selectedPlayer = null;
    private boolean hasMovedCursor = false;
    private Point firstMousePosition = null;

    private ArrayList<DrawButton> buttons = new ArrayList<>();

    public ReportScreen(Screen parent) {
        super(Component.literal("MODQA"));
        this.parent = parent;
        this.animation = new Animation(AnimationStage.OPENING, 0f, EasingFunctions::easeInOutCircular);
        this.playerOutliner = FeatureManager.getFeature(PlayerOutliner.class);
        if (!playerOutliner.getTrackedPlayers().isEmpty()) this.selectedPlayer = playerOutliner.getTrackedPlayers().getFirst();
    }

    private void setSelectedPlayer(String playerName) {
        this.selectedPlayer = playerName;
    }

    @Override
    protected void init() {

    }

    public void draw(GuiGraphics context, int mouseX, int mouseY) {
        Point screen = new Point(Mod.getScaledWindowWidth(), Mod.getScaledWindowHeight());
        int mainContainerWidth = 250;
        int mainContainerHeight = screen.y();
        this.buttons = new ArrayList<>();

        int mainColor = ColorBank.BLACK;
        int buttonColor = 0x1f1f1f;
        int enabledColor = 0x3d3d3d;

        DrawRect mainContainer = new DrawRect(screen.mul(0.5, 0.5).add((int)(50*(1-animation.getProgress())),0), new Point(mainContainerWidth, mainContainerHeight),  new ARGB(ColorBank.BLACK, 0.65f * animation.getProgress()));
        mainContainer.setSelfBinding(new DrawBinding(AxisBinding.MIDDLE, AxisBinding.MIDDLE));

        int margin = 4;
        int reportHeight = ((Mod.MC.font.lineHeight + 1) * 4) + (margin * 2);
        int i = 0;

        for (DatedReport report : FeatureManager.getFeature(ReportScreenFeature.class).reports) {
            String node_formated = report.private_text() + report.node_text() + " " + report.node_number();
            String node_id = report.private_text().isEmpty() ? "node" + report.node_number() : "private" + report.node_number();
            List<Component> reportText = List.of(
                    Component.literal("! Incoming Report (" + report.reporter() + ")"),
                    Component.literal("|  Offender: " + report.offender()),
                    Component.literal("|  Offense: " + report.offense()),
                    Component.literal("|  Location: " + node_formated)
            );

            DrawButton reportContainer = new DrawButton(
                    new Point(0, reportHeight * i),
                    new Point(Mod.MC.font.width(reportText.get(0).getString()), reportHeight),
                    new ARGB(ColorBank.WHITE, 1f),
                    new ARGB(ColorBank.MC_RED, 1f),
                    mainContainer
            );
        }

        mainContainer.render(context, mouseX, mouseY);
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
        if (parent == null) Mod.MC.setScreen((Screen) null);
    }
}
