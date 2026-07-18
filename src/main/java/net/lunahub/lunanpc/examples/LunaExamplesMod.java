package net.lunahub.lunanpc.examples;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Runs the standalone LunaNPC API examples when a world's server starts. Each is a self-contained
// class you can read (or copy into your own mod) on its own; this class only invokes them. Each reuses
// its content if it already exists and re-applies the current settings, so editing an example and
// relaunching always reflects the change, and nothing is ever duplicated.
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
            AdvancedBossExample.build(server);
            DialogTreeExample.build(server);
            RangeSwitchGuardExample.build(server);
            LOGGER.info("LunaNPC examples applied.");
        });
    }
}
