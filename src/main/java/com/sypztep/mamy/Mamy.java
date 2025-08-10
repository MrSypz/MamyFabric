package com.sypztep.mamy;

import com.sypztep.mamy.common.event.living.InitDamageTrackerEvent;
import com.sypztep.mamy.common.event.living.MobDeathExperienceEvent;
import com.sypztep.mamy.common.event.living.MobSpawnStatsEvent;
import com.sypztep.mamy.common.event.living.ModifyLivingDamageEvent;
import com.sypztep.mamy.common.event.player.*;
import com.sypztep.mamy.common.init.ModCommands;
import com.sypztep.mamy.common.init.ModEntityTypes;
import com.sypztep.mamy.common.init.ModParticles;
import com.sypztep.mamy.common.init.ModPayloads;
import com.sypztep.mamy.common.reloadlistener.MamyMobExpReloadListener;
import com.sypztep.mamy.common.system.skill.SkillRegistry;
import net.fabricmc.api.ModInitializer;
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

        SkillRegistry.registerSkills();
        // EVENT
        InitDamageTrackerEvent.register();
        ModifyLivingDamageEvent.register();
        MobSpawnStatsEvent.register();
        MobDeathExperienceEvent.register();
        MarkDamageTrackerEvent.register();
        RestoreStatsModifyEvent.register();
        InitPlayerClassEvent.register();
        PlayerDisconnectCleanupEvent.register();
        DeathPenaltyEvent.register();

        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new MamyMobExpReloadListener());
    }
}
