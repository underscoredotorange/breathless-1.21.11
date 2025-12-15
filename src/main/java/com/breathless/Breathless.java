package com.breathless;

// Default
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Imports

public class Breathless implements ModInitializer {
	public static final String MOD_ID = "breathless";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("[Breathless/Main] Alive and breathing.");
	}
}