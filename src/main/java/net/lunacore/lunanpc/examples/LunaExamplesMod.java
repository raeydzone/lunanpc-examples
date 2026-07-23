package net.lunacore.lunanpc.examples;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Applies the LunaNPC API examples when a world's server starts. Each example is a self-contained class
// you can read on its own or copy into your own mod; this only invokes them. They upsert by name, so
// relaunching re-applies edits without ever duplicating an NPC.
public class LunaExamplesMod implements ModInitializer {

    private static final Logger LOGGER = LoggerFactory.getLogger("LunaNPC-Examples");

    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            SteelGolemExample.build(server);
            ChargerZombieExample.build(server);
            CrimsonBlazeExample.build(server);
            LOGGER.info("LunaNPC examples applied.");
        });
    }
}
