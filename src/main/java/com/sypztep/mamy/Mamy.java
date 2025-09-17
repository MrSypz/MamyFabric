package com.sypztep.mamy;

import com.sypztep.mamy.common.event.living.InitDamageTrackerEvent;
import com.sypztep.mamy.common.event.living.MobDeathExperienceEvent;
import com.sypztep.mamy.common.event.living.MobSpawnStatsEvent;
import com.sypztep.mamy.common.event.player.*;
import com.sypztep.mamy.common.init.*;
import com.sypztep.mamy.common.reloadlistener.MamyElementalReloadListener;
import com.sypztep.mamy.common.reloadlistener.MamyItemWeightReloadListener;
import com.sypztep.mamy.common.reloadlistener.MamyMobExpReloadListener;
import com.sypztep.mamy.common.init.ModClassesSkill;
import com.sypztep.mamy.common.util.TheifDoubleAttackSystem;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resource.ResourceType;
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
        ModEntityTypes.init();
        ModParticles.init();
        ModCustomParticles.init();
        ModClassesSkill.registerSkills();
        ModDataComponents.init();
        ModStatusEffects.init();
        ModItems.init();
        // EVENT
        InitDamageTrackerEvent.register();
        InitPlayerClassEvent.register();
        MobSpawnStatsEvent.register();
        MobDeathExperienceEvent.register();
        MarkDamageTrackerEvent.register();
        RestoreStatsModifyEvent.register();
        PlayerDisconnectCleanupEvent.register();
        DeathPenaltyEvent.register();
        ServerTickEvents.START_SERVER_TICK.register(TheifDoubleAttackSystem::tick);


        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new MamyMobExpReloadListener());
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new MamyElementalReloadListener());
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new MamyItemWeightReloadListener());
    }
}
