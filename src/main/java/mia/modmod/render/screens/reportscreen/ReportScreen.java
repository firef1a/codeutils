package mia.modmod.render.screens.reportscreen;

import mia.modmod.ColorBank;
import mia.modmod.Mod;
import mia.modmod.core.MathUtils;
import mia.modmod.features.FeatureManager;
import mia.modmod.features.impl.moderation.reports.DatedReport;
import mia.modmod.features.impl.moderation.reports.ReportTracker;
import mia.modmod.render.screens.Animation;
import mia.modmod.render.screens.AnimationStage;
import mia.modmod.render.util.*;
import mia.modmod.render.util.elements.*;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ReportScreen extends Screen {
    private final ReportTracker reportTracker;
    private final Screen parent;
    public final Animation animation;

    private ArrayList<DrawButton> buttons = new ArrayList<>();

    private DrawRect reportContainer;
    private ArrayList<DrawButton> reportButtons;

    private double scrollAmount = 0f;
    private double displayScrollAmount = 0f;

    private final int margin = 4;
    private final int lineHeight = (Mod.MC.font.lineHeight + 1);
    private final int reportHeight = (lineHeight * 5) + (margin * 2);

    private static String FILTER_PATTERN = "";
    private MultiLineEditBox filterWidget;



    public ReportScreen(Screen parent) {
        super(Component.literal("REPORT_SCREEN"));
        this.parent = parent;
        this.animation = new Animation(AnimationStage.OPENING, 0f, EasingFunctions::easeInOutCircular);
        this.reportTracker = FeatureManager.getFeature(ReportTracker.class);
    }

    @Override
    protected void init() {
        filterWidget = MultiLineEditBox.builder()
                .setX(500)
                .setY(500)
                .setPlaceholder(Component.literal("filter reports...").withColor(ColorBank.MC_GRAY).withStyle(ChatFormatting.ITALIC))
                .setShowBackground(true)
                .build(Mod.MC.font, 250 - margin * 2, Mod.MC.font.lineHeight * 2 - 1, Component.empty());
        filterWidget.setAlpha(0f);
        addRenderableWidget(filterWidget);
    }

    private boolean filterReport(DatedReport report, String filter) {
        for (String data : new String[] { report.reporter(), report.offender(), report.offense(), report.formattedLocation(), report.mode() }) {
            if (data.toLowerCase(Locale.ROOT).contains(filter.toLowerCase(Locale.ROOT))) return true;
        }
        return false;
    }

    private double reportDelta() {
        return (reportButtons.size() * (reportHeight + 1)) - reportContainer.getHeight();
    }

    public void draw(GuiGraphics context, int mouseX, int mouseY) {
        Point screen = new Point(Mod.getScaledWindowWidth(), Mod.getScaledWindowHeight());

        int mainContainerWidth = 250;
        int mainContainerHeight = screen.y();
        this.buttons = new ArrayList<>();


        int mainColor = ColorBank.BLACK;
        int buttonColor = 0x1f1f1f;
        int enabledColor = 0x3d3d3d;

        DrawRect mainContainer = new DrawRect(screen.mul(0.5, 0.5).add((int)(50*(1-animation.getProgress())),0), new Point(mainContainerWidth, mainContainerHeight),  new ARGB(ColorBank.BLACK, 0.0f * animation.getProgress()));
        mainContainer.setSelfBinding(new DrawBinding(AxisBinding.MIDDLE, AxisBinding.MIDDLE));

        DrawRect titleContainer = new DrawRect(
                new Point(0, margin),
                new Point(mainContainer.getWidth(), lineHeight * 4),
                new ARGB(mainColor, 0.45f * animation.getProgress()),
                mainContainer
        );

        DrawText title = new DrawText(
                new Point(margin, margin),
                Component.literal("Recent Reports:").withColor(ColorBank.WHITE),
                animation.getProgress(),
                true,
                titleContainer
        );

        reportContainer = new DrawRect(
                new Point(0, margin),
                new Point(mainContainer.getWidth(),  (screen.y() - (margin + titleContainer.y2()))),
                new ARGB(mainColor, 0.0f * animation.getProgress()),
                titleContainer
        )  {
            @Override
            public void render(GuiGraphics context, int mouseX, int mouseY) {
                if (this.y2() >= reportContainer.y1()) {
                    context.enableScissor(this.x1(), this.y1(), this.x2(), this.y2());
                    super.render(context, mouseX, mouseY);
                    context.disableScissor();
                }
                /*
                if (containsPoint(mouseX, mouseY)) {
                    //DrawContextHelper.drawTooltip(context, optionInfo, mouseX, mouseY, 0);
                    customToolTip[0] = new DrawCustomToolTip(new Point(mouseX, mouseY), optionInfo, 0);
                }
                 */
            }
        };
        reportContainer.setParentBinding(new DrawBinding(AxisBinding.NONE, AxisBinding.FULL));

        filterWidget.setAlpha(animation.getProgress());
        filterWidget.setX(title.x1());
        filterWidget.setY(title.y2() + margin);


        reportButtons = new ArrayList<>();
        int i = 0;

        for (DatedReport report : FeatureManager.getFeature(ReportTracker.class).reports) {
            if (!filterReport(report, filterWidget.getValue())) continue;
            String node_id = report.private_text().isEmpty() ? "node" + report.node_number() : "private" + report.node_number();
            List<Component> reportText = List.of(
                    Component.empty().append(Component.literal("Reported: ").withColor(0xFFAAAA).append(Component.literal(MathUtils.convertTimestampRelativeHMS(System.currentTimeMillis() - (Math.round(report.timestamp() / 1000.0) * 1000)) + " ago").withColor(ColorBank.WHITE_GRAY))),
                    Component.empty().append(Component.literal("!").withColor(0xFF2A00).withStyle(ChatFormatting.BOLD)).append(Component.literal(" Incoming Report ").withColor(0xFFAAAA)).append(Component.literal("(" + report.reporter() +")").withColor(ColorBank.MC_GRAY)),
                    Component.empty().append(Component.literal("|").withColor(ColorBank.MC_RED).append(Component.literal("  Offender: ").withColor(0xD4D4D4)).append(Component.literal(report.offender()).withColor(ColorBank.WHITE))),
                    Component.empty().append(Component.literal("|").withColor(ColorBank.MC_RED).append(Component.literal("  Offense: ").withColor(0xD4D4D4).append(Component.literal(report.offense()).withColor(ColorBank.WHITE)))),
                    Component.empty().append(Component.literal("|").withColor(ColorBank.MC_RED).append(Component.literal("  Location: ").withColor(0xD4D4D4).append(Component.literal(report.formattedLocation()).withColor(ColorBank.WHITE))))
                    );

            DrawButton reportButton = new DrawButton(
                    new Point(0, ((reportHeight+1) * i)  - ((int) displayScrollAmount)),
                    new Point(mainContainer.getWidth(), reportHeight),
                    new ARGB(ColorBank.BLACK, 0.5f * animation.getProgress()),
                    new ARGB(enabledColor, 0.7f * animation.getProgress()),
                    reportContainer
            );
            reportButtons.add(reportButton);

            DrawRect reportBottom = new DrawRect(
                    new Point(0,0),
                    new Point(reportButton.getWidth(), 1),
                    new ARGB(ColorBank.WHITE_GRAY, 0.25f  * animation.getProgress()),
                    reportButton
            );
            reportBottom.setParentBinding(new DrawBinding(AxisBinding.NONE, AxisBinding.FULL));
            reportBottom.setSelfBinding(new DrawBinding(AxisBinding.NONE, AxisBinding.FULL));


            Component statusText = report.handled() ? Component.literal("Handled").withColor(ColorBank.MC_GREEN) : Component.literal("Unhandled").withColor(0xFF2A00);
            DrawText handledStatus = new DrawText(
                    new Point(-margin, margin),
                    Component.literal("Status: ").append(statusText).withColor(0xFFAAAA),
                    animation.getProgress(),
                    true,
                    reportButton
            );
            handledStatus.setParentBinding(new DrawBinding(AxisBinding.FULL, AxisBinding.NONE));
            handledStatus.setSelfBinding(new DrawBinding(AxisBinding.FULL, AxisBinding.NONE));

            int j = 0;
            for (Component line : reportText) {
                DrawText lineText = new DrawText(
                        new Point(margin, margin + (lineHeight * (j))),
                        line,
                        animation.getProgress(),
                        true,
                        reportButton
                );
                j++;
            }

            i++;
        }

        displayScrollAmount = (displayScrollAmount) + ((scrollAmount - displayScrollAmount) / 1.5);
        mainContainer.render(context, mouseX, mouseY);
        updateAnimation();
    }

    private void updateAnimation() {
        animation.updateAnimation(0.05f);
    }

    @Override
    public void render(@NonNull GuiGraphics context, int mouseX, int mouseY, float delta) {
        if (parent != null) parent.render(context, Integer.MIN_VALUE, Integer.MIN_VALUE, delta);
        draw(context, mouseX, mouseY);

        super.render(context, mouseX, mouseY, delta);
    }


    @Override
    public void renderBackground(@NonNull GuiGraphics context, int mouseX, int mouseY, float delta) { }


    @Override
    public boolean mouseClicked(@NonNull MouseButtonEvent click, boolean doubled) {
        for (DrawButton button : buttons) {
            if (reportButtons.contains(button) && button.y2() < reportContainer.y1()) continue;
            button.mouseClick(click, doubled);
        }
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double dx, double dy) {
        scrollAmount += (dy * -25);
        scrollAmount = Math.clamp(
                scrollAmount,
                0,
                Math.max(
                        0,
                        reportDelta()
                )
        );
        return super.mouseScrolled(mouseX, mouseY, dx, dy);
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
