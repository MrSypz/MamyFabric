package com.sypztep.mamy;

import com.sypztep.mamy.common.api.entity.DominatusLivingEntityEvents;
import com.sypztep.mamy.common.event.living.InitDamageTrackerEvent;
import com.sypztep.mamy.common.event.living.ModifyLivingDamageEvent;
import com.sypztep.mamy.common.event.player.MarkDamageTrackerEvent;
import com.sypztep.mamy.common.event.player.RestoreStatsModifyEvent;
import com.sypztep.mamy.common.init.ModCommands;
import com.sypztep.mamy.common.init.ModPayloads;
import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Mamy implements ModInitializer {
    public static final String MODID = "mamy";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);
    public static Identifier id(String path) {
        return Identifier.of(MODID, path);
    }


    @Override
    public void onInitialize() {
        ModPayloads.init();
        ModCommands.init();

        InitDamageTrackerEvent.register();
        ModifyLivingDamageEvent.register();
        MarkDamageTrackerEvent.register();
        RestoreStatsModifyEvent.register();
    }
}
