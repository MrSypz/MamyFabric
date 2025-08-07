package com.sypztep.mamy.common.compat;

import com.sypztep.mamy.common.compat.provider.EntityComponentProvider;
import net.minecraft.entity.LivingEntity;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;

public class JadePlugin implements IWailaPlugin {
    @Override
    public void register(IWailaCommonRegistration registration) {
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerEntityComponent(EntityComponentProvider.INSTANCE, LivingEntity.class);
    }
}
