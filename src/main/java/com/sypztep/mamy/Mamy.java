package com.sypztep.mamy;

import com.sypztep.mamy.common.event.living.InitDamageTrackerEvent;
import com.sypztep.mamy.common.event.living.MobDeathExperienceEvent;
import com.sypztep.mamy.common.event.living.MobSpawnStatsEvent;
import com.sypztep.mamy.common.event.living.ModifyLivingDamageEvent;
import com.sypztep.mamy.common.event.player.InitPlayerClassEvent;
import com.sypztep.mamy.common.event.player.MarkDamageTrackerEvent;
import com.sypztep.mamy.common.event.player.RestoreStatsModifyEvent;
import com.sypztep.mamy.common.init.ModCommands;
import com.sypztep.mamy.common.init.ModEntityTypes;
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

        SkillRegistry.registerSkills();

        InitDamageTrackerEvent.register();
        ModifyLivingDamageEvent.register();
        MobSpawnStatsEvent.register();
        MobDeathExperienceEvent.register();
        MarkDamageTrackerEvent.register();
        RestoreStatsModifyEvent.register();
        InitPlayerClassEvent.register();

        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new MamyMobExpReloadListener());

    }
}
