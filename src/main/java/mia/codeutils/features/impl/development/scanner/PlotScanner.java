package mia.codeutils.features.impl.development.scanner;

import com.google.gson.JsonArray;
import com.mojang.brigadier.CommandDispatcher;
import mia.codeutils.ColorBank;
import mia.codeutils.Mod;
import mia.codeutils.core.*;
import mia.codeutils.core.items.DFItem;
import mia.codeutils.features.Categories;
import mia.codeutils.features.Feature;
import mia.codeutils.features.impl.internal.commands.ChatConsumer;
import mia.codeutils.features.impl.internal.commands.CommandScheduler;
import mia.codeutils.features.impl.internal.commands.ScheduledCommand;
import mia.codeutils.features.impl.internal.mode.LocationAPI;
import mia.codeutils.features.listeners.DFMode;
import mia.codeutils.features.listeners.ModifiableEventData;
import mia.codeutils.features.listeners.ModifiableEventResult;
import mia.codeutils.features.listeners.impl.*;
import mia.codeutils.render.screens.PlotScanScreen;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.loader.impl.lib.sat4j.core.Vec;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.network.protocol.game.ServerboundPlayerAbilitiesPacket;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

public final class PlotScanner extends Feature implements PacketListener, TickEvent, RegisterCommandListener, ChatEventListener, ServerConnectionEventListener {
    public static final int format_version = 2;
    public static final String folder = "plotscan";
    public PlotData plot;

    public PlotScanningMode plotScanningMode;
    public CodeGrabbingMode codeGrabbingMode;

    public ArrayList<String> receivedEventTypesCTPSuggestion;
    public HashMap<String,ArrayList<String>> receivedCTPLines;
    public ArrayList<CodeTeleportRequest> codeTeleportRequests;
    public ArrayList<CodeTemplateData> codeTemplateData;
    public Vec3i templateLocation;

    private PlotScanScreen screen;


    public final Map<Integer, String> ctpIDMap = new HashMap<>(Map.ofEntries(
            Map.entry(5000001, "event"),
            Map.entry(5000002, "function"),
            Map.entry(5000003, "process")
    ));

    public PlotScanner(Categories category){
        super(category, "Plot Scanner", "plotscanner", "hit netflix tv show pantheon reference?");
        plotScanningMode = PlotScanningMode.NONE;
        codeGrabbingMode = CodeGrabbingMode.AVAILABLE;
    }

    public enum PlotScanningMode {
        NONE,
        REQUESTING_LOCATE_INFO,
        PTP_WAITING,
        REQUESTING_CODE_LINES,
        GRABBING_CODE
    }

    public enum CodeGrabbingMode {
        AVAILABLE,
        AWAIT_TELEPORT,
        TELEPORTED,
        AWAIT_GRAB_TEMPLATE
    }

    public record CodeTeleportRequest(String eventType, String lineName) { }

    private void startScan() {
        plotScanningMode = PlotScanningMode.REQUESTING_CODE_LINES;

        Mod.message(Component.literal("Starting scan for plot: ").append(Component.literal(plot.plotName + " [" + plot.plotId + "]").withColor(ColorBank.WHITE_GRAY)));
        Mod.message(Component.empty()
                .append(Component.literal("If the scan hangs or gets stuck due to invalid code, use ").withColor(ColorBank.WHITE))
                .append(Component.literal("/killscan").withColor(ColorBank.WHITE_GRAY))
                .append(Component.literal(" to kill it.").withColor(ColorBank.WHITE))

        );

        ArrayList<ServerboundCommandSuggestionPacket> requestCommandCompletionsC2SPackets = new ArrayList<>(List.of(
                new ServerboundCommandSuggestionPacket(5000001, "/ctp event "),
                new ServerboundCommandSuggestionPacket(5000002, "/ctp function "),
                new ServerboundCommandSuggestionPacket(5000003, "/ctp process ")
        ));
        for (ServerboundCommandSuggestionPacket p : requestCommandCompletionsC2SPackets) { NetworkManager.sendPacket(p); }

        receivedEventTypesCTPSuggestion = new ArrayList<>();
        receivedCTPLines = new HashMap<>();
        codeTeleportRequests = new ArrayList<>();
        codeTemplateData = new ArrayList<>();
        for (String eventName : ctpIDMap.values()) { receivedCTPLines.put(eventName, new ArrayList<>()); }
    }

    private void finishScan() {
        plotScanningMode = PlotScanningMode.NONE;
        ScheduledCommand positionCommand = new ScheduledCommand("ptp -1 52 0", 0L, List.of());
        CommandScheduler.addCommand(positionCommand);
        saveData();
    }

    private void saveData() {
        FileManager.createFolder(folder);

        // source
        String pathRaw = folder + "/" + plot.getSaveFileName() + ".plot";
        JsonArray jsonCodeTemplates = new JsonArray();

        for (CodeTemplateData codeTemplate : codeTemplateData) {
            String stringLocation = String.format("%s,%s,%s", codeTemplate.location.getX(), codeTemplate.location.getY(), codeTemplate.location.getZ());
            String code = codeTemplate.code;

            JsonObject jsonLine = new JsonObject();
            jsonLine.addProperty("loc", stringLocation);
            jsonLine.addProperty("code", code);

            try {
                jsonLine.addProperty("deflated", GzipUtils.decompress(Base64Utils.decodeBase64Bytes(code)));
            } catch (IOException e) {
                jsonLine.addProperty("deflated", "$INVALID_TEMPLATE");
                Mod.messageError(stringLocation + ": " + "$INVALID_TEMPLATE");
            }
            jsonCodeTemplates.add(jsonLine);
        }
        JsonObject jsonMainBody = new JsonObject();

        jsonMainBody.addProperty("format_version", format_version);
        jsonMainBody.addProperty("plot_owner", plot.owner);
        jsonMainBody.addProperty("plot_name", plot.plotName);
        jsonMainBody.addProperty("plot_id", plot.plotId);
        jsonMainBody.addProperty("plot_node", plot.node);
        jsonMainBody.add("templates", jsonCodeTemplates);

        try {
            FileManager.writeFile(FileManager.getPath(pathRaw), Mod.gson.toJson(jsonMainBody));
            Mod.message(Component.literal("wrote: ").append(Component.literal(pathRaw).withColor(ColorBank.WHITE_GRAY)));
        } catch (IOException e) {
            Mod.messageError(e.getMessage());
        }
    }

    private void killScan() {
        plotScanningMode = PlotScanningMode.NONE;
    }

    @Override
    public void receivePacket(Packet<?> packet, CallbackInfo ci) {
        if (Mod.MC.getConnection() == null) return;
        if (Mod.MC.player == null) return;

        // PTP start & on /ctp teleport to codeline
        if (packet instanceof ClientboundPlayerPositionPacket playerPositionLookS2CPacket) {
            Vec3 pos3d = playerPositionLookS2CPacket.change().position();
            Vec3i pos3i = new Vec3i((int) pos3d.x, (int) pos3d.y, (int) pos3d.z);

            if (LocationAPI.getMode().equals(DFMode.DEV)) {
                if (plotScanningMode.equals(PlotScanningMode.GRABBING_CODE) && codeGrabbingMode.equals(CodeGrabbingMode.AWAIT_TELEPORT)) {
                    templateLocation = pos3i.offset(0, -2, 0);
                    codeGrabbingMode = CodeGrabbingMode.TELEPORTED;
                }


                if (plotScanningMode.equals(PlotScanningMode.PTP_WAITING)) {
                    plot.setPlotBase(new Vec3i((int) pos3d.x, (int) pos3d.y - 2, (int) pos3d.z));
                    Mod.message(Component.literal("plot origin: ").append(Component.literal(plot.plotBase.toShortString()).withColor(ColorBank.WHITE_GRAY)));
                    startScan();
                }
            }
        }

        // CTP suggestion results
        if (plotScanningMode.equals(PlotScanningMode.REQUESTING_CODE_LINES)) {
            if (packet instanceof ClientboundCommandSuggestionsPacket(int syncId, int start, int length, List<ClientboundCommandSuggestionsPacket.Entry> suggestions)) {
                if (ctpIDMap.containsKey(syncId)) {
                    String eventName = ctpIDMap.get(syncId);
                    receivedEventTypesCTPSuggestion.add(eventName);

                    for (ClientboundCommandSuggestionsPacket.Entry suggestion : suggestions) {
                        String lineName = suggestion.text();
                        receivedCTPLines.get(eventName).add(lineName);
                    }

                    Mod.messageError("packet received: " + eventName);

                    // check if it has received all 3 types then start scanning
                    if (receivedEventTypesCTPSuggestion.size() == ctpIDMap.size()) {

                        Mod.message(
                                Component.empty()
                                        .append(Component.literal("Plot scan started ").withColor(ColorBank.WHITE))
                                        .append(Component.literal("(" + getTotalLines() + " lines)").withColor(ColorBank.WHITE_GRAY))
                        );

                        codeTeleportRequests = new ArrayList<>();
                        long totalScanDelay = 0L;

                        for (String eventType : receivedCTPLines.keySet()) {
                            ArrayList<String> lineData = receivedCTPLines.get(eventType);

                            for (String lineName : lineData) {
                                codeTeleportRequests.add(new CodeTeleportRequest(eventType, lineName));
                                totalScanDelay += getLineCTPCommand(eventType, lineName).getDelay();
                            }
                        }

                        Mod.message(
                                Component.literal("ETA: ")
                                        .append(Component.literal(MathUtils.roundToDecimalPlaces((double) (totalScanDelay) / (1000.0), 2) + "s").withColor(ColorBank.WHITE_GRAY))
                        );

                        plotScanningMode = PlotScanningMode.GRABBING_CODE;
                        codeGrabbingMode = CodeGrabbingMode.AVAILABLE;
                    }
                }
            }
        }

        // receive codetemplate
        if (codeGrabbingMode.equals(CodeGrabbingMode.AWAIT_GRAB_TEMPLATE)) {
            if (packet instanceof ClientboundContainerSetSlotPacket slot) {
                Optional<HashMap<String, Tag>> hypercubeTags = new DFItem(slot.getItem()).getHypercubeItemTags(false);
                if (hypercubeTags.isPresent()) {
                   HashMap<String, Tag> tags = hypercubeTags.get();
                   if (tags.containsKey("codetemplatedata")) {
                       String codetemplate = tags.get("codetemplatedata").asString().get();

                       InternalCodeTemplateData internalCodeTemplateData = Mod.gson.fromJson(codetemplate, InternalCodeTemplateData.class);
                       codeTemplateData.add(new CodeTemplateData(internalCodeTemplateData, templateLocation.subtract(plot.plotBase)));

                       Mod.message(Component.literal("Scanned line ").append(Component.literal(codeTemplateData.size() + "/" + getTotalLines()).withColor(ColorBank.WHITE_GRAY)));
                       NetworkManager.sendPacket(new ServerboundSetCreativeModeSlotPacket(slot.getSlot(), ItemStack.EMPTY));

                       codeGrabbingMode = CodeGrabbingMode.AVAILABLE;
                       if (codeTeleportRequests.isEmpty()) finishScan();
                   }
                }
            }
        }
    }

    private int getTotalLines() {
        int totalLines = 0;
        for (ArrayList<String> lines : receivedCTPLines.values()) totalLines += lines.size();
        return totalLines;
    }

    private record InternalCodeTemplateData(String author, String name, Integer version, String code) {}
    private record CodeTemplateData(String author, String name, Integer version, String code, Vec3i location) {
        public CodeTemplateData(InternalCodeTemplateData template, Vec3i location) {
            this(template.author, template.name, template.version, template.code, location);
        }
    }

    private ScheduledCommand getLineCTPCommand(String eventType, String lineName) {
        return new ScheduledCommand("ctp " + eventType + " " + lineName);
    }

    @Override
    public void register(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext registryAccess) {
        dispatcher.register(ClientCommandManager.literal("plotscan")
                .executes(commandContext -> {
                    if (LocationAPI.getMode().equals(DFMode.DEV)) {
                        if (plotScanningMode.equals(PlotScanningMode.NONE)) {
                            plotScanningMode = PlotScanningMode.REQUESTING_LOCATE_INFO;
                            ScheduledCommand scheduledCommand = new ScheduledCommand("locate", 0L, List.of(
                                    new ChatConsumer(
                                            Pattern.compile("^ {39}\\nYou are currently coding on:\\n\\n→ (.*) \\[([0-9]+)] (?:|\\[.*])\\n→ Owner: ([a-zA-Z0-9_]{3,16}) (?:|§8\\[§7Whitelisted§8])\\n→ Server: (.*)\\n {39}"),
                                            matcher -> {
                                                String plotName = matcher.group(1);
                                                int plotID = Integer.parseInt(matcher.group(2));
                                                String plotOwner = matcher.group(3);
                                                String plotNode = matcher.group(4);
                                                plot = new PlotData(
                                                        plotOwner,
                                                        plotName,
                                                        plotID,
                                                        plotNode
                                                );
                                                Mod.message(plotOwner + " " + plotName + " " + plotID + " " + plotNode);
                                                plotScanningMode = PlotScanningMode.PTP_WAITING;
                                                ScheduledCommand positionCommand = new ScheduledCommand("ptp -1 52 0", 0L, List.of());
                                                CommandScheduler.addCommand(positionCommand);
                                            },
                                            () -> {
                                                Mod.messageError("Failed to grab plot id, try again later...");
                                                killScan();
                                            },
                                            10000L,
                                            true
                                    )
                            ));
                            CommandScheduler.addCommand(scheduledCommand);
                            Mod.setCurrentScreen(screen = new PlotScanScreen(null, this));
                        } else {
                            Mod.messageError("Plot scan already in progress, use /killscan to forcefully kill the current scan.");
                        }
                    } else {
                        Mod.messageError("You must be in dev mode to use this feature.");
                    }
                    return 1;
                })
        );

        dispatcher.register(ClientCommandManager.literal("killscan")
                .executes(commandContext -> {
                    if (LocationAPI.getMode().equals(DFMode.DEV)) {
                        if (!plotScanningMode.equals(PlotScanningMode.NONE)) {
                            Mod.message("Killed current plot scan.");
                            killScan();
                        } else {
                            Mod.messageError("No active plot scan to kill.");
                        }
                    } else {
                        Mod.messageError("You must be in dev mode to use this feature.");
                    }
                    return 1;
                })
        );
    }

    @Override
    public ModifiableEventResult<Component> chatEvent(ModifiableEventData<Component> message, CallbackInfo ci) {
        if (plotScanningMode.equals(PlotScanningMode.GRABBING_CODE)) {
            if (codeGrabbingMode != null && codeGrabbingMode.equals(CodeGrabbingMode.AWAIT_GRAB_TEMPLATE)) {
                if (message.base().getString().equals("Error: Unable to create code template! Exceeded the code data size limit.")) {
                    codeGrabbingMode = CodeGrabbingMode.AVAILABLE;
                    Mod.messageError(Component.literal("Failed to grab code template, total template amount is now ").append(Component.literal("" + getTotalLines()).withColor(ColorBank.WHITE_GRAY)));
                }
            }
            if (message.base().getString().equals("Note: You can view your past 5 created templates with /templatehistory!")) {
                ci.cancel();
            }

        }
        return message.pass();
    }

    @Override
    public void sendPacket(Packet<?> packet, CallbackInfo ci) {

    }

    @Override
    public void tickR(int tick) {
        if (Mod.MC.player == null) return;

        if (plotScanningMode.equals(PlotScanningMode.GRABBING_CODE)) {
            if (codeGrabbingMode.equals(CodeGrabbingMode.AVAILABLE) && !codeTeleportRequests.isEmpty()) {
                CodeTeleportRequest codeTeleportRequest = codeTeleportRequests.removeFirst();
                CommandScheduler.addCommand(getLineCTPCommand(codeTeleportRequest.eventType(), codeTeleportRequest.lineName()));
                codeGrabbingMode = CodeGrabbingMode.AWAIT_TELEPORT;
            }
            if (codeGrabbingMode.equals(CodeGrabbingMode.TELEPORTED)) {
                codeGrabbingMode = CodeGrabbingMode.AWAIT_GRAB_TEMPLATE;

                BlockHitResult hit = new BlockHitResult(new Vec3(templateLocation), Direction.WEST, new BlockPos(templateLocation), false);

                Mod.MC.player.setShiftKeyDown(true);
                NetworkManager.sendPacket(new ServerboundPlayerInputPacket(new Input(false, false, false, false, false, true, false)));
                NetworkManager.sendPacket(new ServerboundUseItemOnPacket(InteractionHand.MAIN_HAND, hit, 10));
                NetworkManager.sendPacket(new ServerboundPlayerInputPacket(new Input(false, false, false, false, false, false, false)));

                Mod.MC.player.setShiftKeyDown(false);
            }
        }
    }

    @Override
    public void tickF(int tick) {

    }

    @Override
    public void serverConnectInit(ClientPacketListener networkHandler, Minecraft minecraftServer) {

    }

    @Override
    public void serverConnectJoin(ClientPacketListener networkHandler, PacketSender sender, Minecraft minecraftServer) {
        plotScanningMode = PlotScanningMode.NONE;
        codeGrabbingMode = CodeGrabbingMode.AVAILABLE;
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
}
