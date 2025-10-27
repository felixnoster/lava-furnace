package com.example.lavafurnace;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LavaFurnaceMod implements ModInitializer {
    public static final String MODID = "lavafurnace";
    public static final Logger LOG = LoggerFactory.getLogger("LavaFurnace");

    @Override
    public void onInitialize() {
        LOG.info("[LavaFurnace] Loaded. Mode: singleplayer-only, target=FURNACE, radius=4, requireInput=true");
    }
}
