package mia.codeutils.features.impl.support.hud;

import mia.codeutils.ColorBank;
import mia.codeutils.Mod;
import mia.codeutils.core.MathUtils;
import mia.codeutils.features.Categories;
import mia.codeutils.features.Feature;
import mia.codeutils.features.listeners.ModifiableEventData;
import mia.codeutils.features.listeners.ModifiableEventResult;
import mia.codeutils.features.listeners.impl.ChatEventListener;
import mia.codeutils.features.listeners.impl.RenderHUD;
import mia.codeutils.features.listeners.impl.ServerConnectionEventListener;
import mia.codeutils.features.listeners.impl.TickEvent;
import mia.codeutils.render.util.ARGB;
import mia.codeutils.render.util.AxisBinding;
import mia.codeutils.render.util.DrawBinding;
import mia.codeutils.render.util.Point;
import mia.codeutils.render.util.elements.DrawRect;
import mia.codeutils.render.util.elements.DrawText;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class SupportHUD extends Feature implements RenderHUD, ChatEventListener, ServerConnectionEventListener, TickEvent {
    private static final String queueHeader = "» Current Queue:";

    private static final LinkedHashMap<String, SessionEntry> sessionQueue = new LinkedHashMap<>();
    private static final LinkedHashMap<String, SupportQuestionEntry> questionQueue = new LinkedHashMap<>();

    private static SessionEntry currentSupportSession = null;
    private static SessionEntry sessionBuilder;

    private static DrawRect supportHUDContainer;

    public SupportHUD(Categories category) {
        super(category, "Support HUD", "supporthud", "get back to work wageslave, put on that customer service smile and earn those sparks!");

    }

    @Override
    public ModifiableEventResult<Component> chatEvent(ModifiableEventData<Component> message, CallbackInfo ci) {
        String text = message.base().getString();
        String playerName = Mod.getPlayerName();
        Matcher matcher;

        // this used to allow u to click on support sessions to join them but the devs added that feature :3

        // queue command response
        if (text.startsWith(queueHeader)) { sessionQueue.clear(); }

        matcher = Pattern.compile("^#\\d* (.{3,16}) ▶ (\\d*):(\\d*):(\\d*)").matcher(text);
        if (matcher.find()) {
            sessionBuilder = new SessionEntry(matcher.group(1), Integer.parseInt(matcher.group(2)), Integer.parseInt(matcher.group(3)) ,Integer.parseInt(matcher.group(4)));
        }

        matcher = Pattern.compile("^ {2}▶ Reason: (.*)").matcher(text);
        if (matcher.find()) {
            if (sessionBuilder != null) {
                sessionBuilder.setReason(matcher.group(1));
                sessionQueue.put(sessionBuilder.name, sessionBuilder);
            }
        }


        // queue missing kill event
        matcher = Pattern.compile("^\\[SUPPORT] (.{3,16}) joined the support queue\\. ▶ Reason: (.*) \\[ACCEPT]").matcher(text);
        if (matcher.find()) {
            sessionQueue.put(matcher.group(1), new SessionEntry(matcher.group(1), matcher.group(2), System.currentTimeMillis()));
        }

        matcher = Pattern.compile("^\\[SUPPORT] (.{3,16}) left the support queue\\.").matcher(text);
        if (matcher.find()) {
            sessionQueue.remove(matcher.group(1));
        }

        matcher = Pattern.compile("^\\[SUPPORT] (.{3,16}) entered a session with (.{3,16})\\..*").matcher(text);
        if (matcher.find()) {
            String supportName = matcher.group(1);
            String supporteeName = matcher.group(2);
            //Mod.log(matcher.group(1) + " " + playerName);
            if (supportName.equals(playerName)) {
                currentSupportSession = sessionQueue.getOrDefault(supporteeName, new SessionEntry(supporteeName, "failed to grab reason :/", System.currentTimeMillis()));
                currentSupportSession.timestamp = System.currentTimeMillis();
            }

            sessionQueue.remove(supporteeName);
        }

        matcher = Pattern.compile("^\\[SUPPORT] (.{3,16}) terminated a session with (.{3,16})\\. ▶ .*").matcher(text);
        if (matcher.find() && matcher.group(1).equals(playerName)) {currentSupportSession = null;}

        matcher = Pattern.compile("^\\[SUPPORT] (.{3,16}) finished a session with (.{3,16})\\. ▶ .*").matcher(text);
        if (matcher.find() && matcher.group(1).equals(playerName)) {currentSupportSession = null;}

        // questions unfinished
        matcher = Pattern.compile("^» Support Question: \\(Click to answer\\)\\nAsked by (.{3,16}) (.{3,16})\\n(.*)").matcher(text);
        if (matcher.find()) {
            questionQueue.put(matcher.group(1), new SupportQuestionEntry(matcher.group(1), matcher.group(2), matcher.group(3)));
        }

       // matcher = Pattern.compile("^ {39}\\n» (.{3,16}) has answered (.{3,16})'(?:s|) question:\\n\\n[A-Za-z0-9_ ]*\\n {39}").matcher(text);
        matcher = Pattern.compile("^ {39}\\n» (.{3,16}) has answered (.{3,16})'(?:s|) question:\\n\\n(.*)\\n").matcher(text);
        if (matcher.find()) { questionQueue.remove(matcher.group(2)); }

        return message.pass();
    }

    @Override
    public void renderHUD(GuiGraphics context, DeltaTracker tickCounter) {
        int containerMargin = 3;
        int lineSpacing = 1;
        int maxTextWidth = 300;
        int screenEdgeMargin = 5;

        ArrayList<ArrayList<Component>> supportTextLists = new ArrayList<ArrayList<Component>>();

        // CURRENT
        if (!(currentSupportSession == null)) {
            ArrayList<Component> textList = new ArrayList<>();
            //textList.add(Component.literal("cᴜʀʀᴇɴᴛ ꜱᴇꜱꜱɪᴏɴ:"));

            String HMSTimestamp = MathUtils.convertTimestampToHMS(System.currentTimeMillis() - currentSupportSession.timestamp);
            textList.add(

                    Component.literal("cᴜʀʀᴇɴᴛ ꜱᴇꜱꜱɪᴏɴ:").withColor(ColorBank.WHITE_GRAY)
                            .append(
                                    Component.literal(" " + currentSupportSession.name).withColor(0xa6aaff)
                                            .append(
                                                    Component.literal(" (" + HMSTimestamp + ")").withColor(ColorBank.WHITE_GRAY)
                                            )
                            )
            );

            textList.add(
                    Component.literal(" ▶ ").withColor(0xc2c5ff)
                            .append(
                                    Component.literal(currentSupportSession.reason).withColor(ColorBank.WHITE)
                            )
            );


            int maxComponentWidth = 0;
            for (Component component : textList) {
                int w = Mod.MC.font.width(component.getString());
                if (w > maxComponentWidth) maxComponentWidth = w;
            }

            supportTextLists.add(textList);
        }
        // SESSIONS
        if (!sessionQueue.isEmpty()) {
            ArrayList<Component> textList = new ArrayList<>();
            //textList.add(Component.literal("qᴜᴇᴜᴇ:"));

            int i = 0;
            for (SessionEntry sessionEntry : sessionQueue.values()) {
                i++;
                String HMSTimestamp = MathUtils.convertTimestampToHMS(System.currentTimeMillis() - sessionEntry.timestamp);
                textList.add(
                        Component.literal("#" + i).withColor(ColorBank.MC_GRAY)
                                .append(
                                        Component.literal(" " + sessionEntry.name).withColor(0xa6aaff)
                                                .append(
                                                        Component.literal(" (" + HMSTimestamp + ")").withColor(ColorBank.WHITE_GRAY)
                                                )
                                )
                );

                textList.add(
                        Component.literal(" ▶ ").withColor(0xc2c5ff)
                                .append(
                                        Component.literal(sessionEntry.reason).withColor(ColorBank.WHITE)
                                )
                );
            }
            supportTextLists.add(textList);
        }

        // QUESTIONS
        if (!questionQueue.isEmpty()) {
            ArrayList<Component> textList = new ArrayList<>();
            //textList.add(Component.literal("qᴜᴇꜱᴛɪᴏɴꜱ:"));

            int i = 0;
            for (SupportQuestionEntry questionEntry : questionQueue.values()) {
                i++;
                String HMSTimestamp = MathUtils.convertTimestampToHMS(System.currentTimeMillis() - questionEntry.timestamp());
                textList.add(
                        Component.literal(questionEntry.name()).withColor(0x97ff94)
                                .append(
                                        Component.literal(" " + questionEntry.rank() + " ").withColor(ColorBank.MC_GRAY)
                                                .append(
                                                        Component.literal("(" + HMSTimestamp +")").withColor(ColorBank.WHITE_GRAY)
                                                )
                                )
                );
                textList.add(
                        Component.literal(" - ").withColor(ColorBank.MC_GRAY)
                                .append(
                                        Component.literal(questionEntry.message()).withColor(ColorBank.WHITE)
                                )
                );
            }
            supportTextLists.add(textList);
        }

        if (!supportTextLists.isEmpty()) {
            int maxWidth = 0;
            for (ArrayList<Component> components : supportTextLists) {
                for (Component component : components) {
                    int w = Mod.MC.font.width(component.getString());
                    if (w > maxWidth) maxWidth = w;
                }
            }
            int containerWidth = maxWidth + (containerMargin * 2);
            supportHUDContainer = new DrawRect(
                    new Point(Mod.getScaledWindowWidth() - (containerWidth + screenEdgeMargin), screenEdgeMargin),
                    new Point(0, 0),
                    0,
                    new ARGB(0, 0f)
            );

            DrawRect parentContainer = supportHUDContainer;
            int i = 0;
            for (ArrayList<Component> components : supportTextLists) {
                DrawRect container = new DrawRect(
                        new Point(0, i == 0 ? 0 : 2),
                        new Point(containerWidth, (Mod.MC.font.lineHeight * components.size()) + (lineSpacing * (components.size() - 1)) + (containerMargin * 2)),
                        0,
                        new ARGB(ColorBank.BLACK, 0.6f),
                        parentContainer
                );
                container.setParentBinding(new DrawBinding(AxisBinding.NONE, AxisBinding.FULL));
                parentContainer = container;

                int j = 0;
                for (Component component : components) {
                    DrawText line = new DrawText(
                            new Point(containerMargin, containerMargin + ((Mod.MC.font.lineHeight + lineSpacing) * j)),
                            component,
                            0,
                            1f,
                            false,
                            container
                    );
                    j++;
                }
                i++;
            }

            supportHUDContainer.render(context, 0, 0);
        }
    }

    @Override
    public void serverConnectInit(ClientPacketListener networkHandler, Minecraft minecraftServer) {

    }

    @Override
    public void serverConnectJoin(ClientPacketListener networkHandler, PacketSender sender, Minecraft minecraftServer) {
        currentSupportSession = null;
        sessionQueue.clear();
        questionQueue.clear();
    }

    @Override
    public void serverConnectDisconnect(ClientPacketListener networkHandler, Minecraft minecraftServer) {

    }

    @Override
    public void DFConnectJoin(ClientPacketListener networkHandler) {

    }

    @Override
    public void DFConnectDisconnect(ClientPacketListener networkHandler) {

    }

    @Override
    public void tickR(int tick) {
        ArrayList<String> removeQuestions = new ArrayList<>();
        for (String name : questionQueue.keySet()) {
            SupportQuestionEntry entry = questionQueue.get(name);
            if (entry.timestamp() + ((10L * 60L) * 1000L) < System.currentTimeMillis()) {
                Mod.message(entry.name() + "'s question was removed");
                removeQuestions.add(name);
            }
        }
        for (String name : removeQuestions) {
            questionQueue.remove(name);
        }
    }

    @Override
    public void tickF(int tick) {

    }
}