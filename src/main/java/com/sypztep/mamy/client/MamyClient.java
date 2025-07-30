package com.sypztep.mamy.client;

import com.sypztep.mamy.client.event.*;
import com.sypztep.mamy.common.init.ModPayloads;
import net.fabricmc.api.ClientModInitializer;

public class MamyClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ModPayloads.registerClientPayloads();
        ModKeyBindings.register();
        LevelHudRenderer.register();
        ToastHudRenderer.register();
        ScreenEventHandler.register();
        ResourceBarHudRenderer.register();
        SkillHudOverlayRenderer.register();
    }
}
