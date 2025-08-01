package com.sypztep.mamy.client.payload;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.client.toast.ToastManager;
import com.sypztep.mamy.client.toast.ToastNotification;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import sypztep.tyrannus.common.util.NumberUtil;

public record SendToastPayloadS2C(String message, int toastTypeOrdinal) implements CustomPayload {
    public static final Id<SendToastPayloadS2C> ID = new Id<>(Mamy.id("show_toast"));
    public static final PacketCodec<PacketByteBuf, SendToastPayloadS2C> CODEC = PacketCodec.tuple(
            PacketCodecs.STRING,
            SendToastPayloadS2C::message,
            PacketCodecs.VAR_INT,
            SendToastPayloadS2C::toastTypeOrdinal,
            SendToastPayloadS2C::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public static void sendExperience(ServerPlayerEntity player, long amount, String source) {
        String message = source != null ?
                String.format("§l§6⚡ EXPERIENCE GAINED ⚡§r\n§e+%s EXP §8• §7%s", NumberUtil.formatNumber(amount), source) :
                String.format("§l§6⚡ EXPERIENCE GAINED ⚡§r\n§e+%s EXP", NumberUtil.formatNumber(amount));

        send(player, message, ToastNotification.ToastType.EXPERIENCE);
    }

    public static void sendCombinedExperience(ServerPlayerEntity player, long mainExp, long classExp, String source) {
        StringBuilder message = new StringBuilder("§l§6⚡ EXPERIENCE GAINED ⚡§r\n");

        // Add main exp if any
        if (mainExp > 0) {
            message.append("§e+").append(NumberUtil.formatNumber(mainExp)).append(" EXP");
        }

        // Add class exp if any
        if (classExp > 0) {
            if (mainExp > 0) {
                message.append(" §8| ");
            }
            message.append("§b+").append(NumberUtil.formatNumber(classExp)).append(" Class EXP");
        }

        // Add source if provided
        if (source != null) {
            message.append(" §8• §7").append(source);
        }

        send(player, message.toString(), ToastNotification.ToastType.EXPERIENCE);
    }

    public static void sendLevelUp(ServerPlayerEntity player, int levelGained) {
        String message = levelGained > 1 ?
                String.format("§l§6⚔ LEVELS GAINED ⚔§r\n§7You gained §e§l%d§r§7 levels!", levelGained) :
                String.format("§l§6⚔ LEVEL GAINED ⚔§r\n§7You gained §e§l%d§r§7 level!", levelGained);
        send(player, message, ToastNotification.ToastType.LEVEL_UP);
    }

    public static void sendClassLevelUp(ServerPlayerEntity player, int levelGained, String className) {
        String message = levelGained > 1 ?
                String.format("§l§b⚔ CLASS LEVELS GAINED ⚔§r\n§7You gained §b§l%d§r§7 class levels! %s", levelGained, className) :
                String.format("§l§b⚔ CLASS LEVEL GAINED ⚔§r\n§7You gained §b§l%d§r§7 class level! %s", levelGained , className);
        send(player, message, ToastNotification.ToastType.LEVEL_UP);
    }

    public static void sendCombinedLevelUp(ServerPlayerEntity player, int mainLevels, int classLevels) {
        StringBuilder message = new StringBuilder("§l§6⚔ LEVEL UP ⚔§r\n");

        if (mainLevels > 0) {
            message.append("§e+").append(mainLevels).append(" Level");
            if (mainLevels > 1) message.append("s");
        }

        if (classLevels > 0) {
            if (mainLevels > 0) {
                message.append(" §8| ");
            }
            message.append("§b+").append(classLevels).append(" Class Level");
            if (classLevels > 1) message.append("s");
        }

        send(player, message.toString(), ToastNotification.ToastType.LEVEL_UP);
    }

    public static void sendDeathPenalty(ServerPlayerEntity player, long expLost, String killerName) {
        String formattedPenalty = NumberUtil.formatNumber(expLost);
        String message = killerName != null ?
                String.format("§l§8☠ DEATH PENALTY ☠§r\n§7Lost §c%s EXP §8• §7%s", formattedPenalty, killerName) :
                String.format("§l§8☠ DEATH PENALTY ☠§r\n§7Lost §c%s EXP", formattedPenalty);

        send(player, message, ToastNotification.ToastType.DEATH_PENALTY);
    }

    public static void sendStatIncrease(ServerPlayerEntity player, String statName, int points, int cost) {
        String message = String.format("§l§6✦ %s ENHANCED ✦§r\n§7+%d Points §8• §e%d Benefits Spent",
                statName.toUpperCase(), points, cost);
        send(player, message, ToastNotification.ToastType.INFO);
    }

    public static void sendBenefitsGained(ServerPlayerEntity player, int amount, String reason) {
        String message = reason != null ?
                String.format("§l§e⬟ BENEFIT POINTS ⬟§r\n§6+%d Points §8• §7%s", amount, reason) :
                String.format("§l§e⬟ BENEFIT POINTS ⬟§r\n§6+%d Points Gained", amount);

        send(player, message, ToastNotification.ToastType.INFO);
    }

    public static void sendInfo(ServerPlayerEntity player, String message) {
        String formattedMessage = String.format("§l§6◈ NOTICE ◈§r\n§7%s", message);
        send(player, formattedMessage, ToastNotification.ToastType.INFO);
    }

    public static void sendWarning(ServerPlayerEntity player, String message) {
        String formattedMessage = String.format("§l§e⚠ WARNING ⚠§r\n§7%s", message);
        send(player, formattedMessage, ToastNotification.ToastType.WARNING);
    }

    public static void sendError(ServerPlayerEntity player, String message) {
        String formattedMessage = String.format("§l§8✖ ERROR ✖§r\n§7%s", message);
        send(player, formattedMessage, ToastNotification.ToastType.ERROR);
    }

    // Generic send method
    private static void send(ServerPlayerEntity player, String message, ToastNotification.ToastType type) {
        ServerPlayNetworking.send(player, new SendToastPayloadS2C(message, type.ordinal()));
    }

    public static class Receiver implements ClientPlayNetworking.PlayPayloadHandler<SendToastPayloadS2C> {
        @Override
        public void receive(SendToastPayloadS2C payload, ClientPlayNetworking.Context context) {
            ToastNotification.ToastType[] types = ToastNotification.ToastType.values();
            if (payload.toastTypeOrdinal < 0 || payload.toastTypeOrdinal >= types.length) return;

            ToastNotification.ToastType type = types[payload.toastTypeOrdinal];
            Text message = Text.literal(payload.message);

            ToastManager.getInstance().addToast(new ToastNotification(message, type));
        }
    }
}