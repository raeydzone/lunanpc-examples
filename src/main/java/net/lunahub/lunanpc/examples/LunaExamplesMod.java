package net.lunahub.lunanpc.examples;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Runs the five standalone LunaNPC API examples once, when a world's server starts. Each example is
// a self-contained class you can read (or copy into your own mod) on its own — this class only
// invokes them. All five are idempotent, so relaunching a world never piles up duplicates.
public class LunaExamplesMod implements ModInitializer {

    private static final Logger LOGGER = LoggerFactory.getLogger("LunaNPC-Examples");

    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            DungeonLurkerExample.build(server);
            BiomeArcherExample.build(server);
            FriendlyGuardExample.build(server);
            WarringFactionsExample.build(server);
            BossExample.build(server);
            LOGGER.info("LunaNPC examples applied.");
        });
    }
}
