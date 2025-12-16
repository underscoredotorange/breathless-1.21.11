package com.breathless;

// Default
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Imports
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import static net.minecraft.commands.Commands.literal;
import net.minecraft.network.chat.Component;
import java.nio.file.*;

public class Breathless implements ModInitializer {
	// Default
	public static final String MOD_ID = "breathless";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public void log(String message) { LOGGER.info(message); }

	// Added
	int secondsDelayTillNextBreath = 3;
	int ticksTillNextBreath = 1;

	// Persistence
	private String playerDataFile;
	private java.util.HashMap<String, Integer> acclimationCache = new java.util.HashMap<>();

	public void setPlayerInt(ServerPlayer player, int value) {
        acclimationCache.put(player.getUUID().toString(), value);
    	saveAllPlayers();
    }

	public int getPlayerInt(ServerPlayer player) {
        return acclimationCache.getOrDefault(
			player.getUUID().toString(), 0
		);
    }

	private void loadAllPlayers() {
		try {
			java.util.List<String> lines = Files.readAllLines(
				java.nio.file.Path.of(playerDataFile)
			);

			for (String line : lines) {
				if (!line.contains("=")) continue;

				String[] split = line.split("=");
				acclimationCache.put(split[0], Integer.parseInt(split[1]));
			}

			log("[Breathless] Loaded " + acclimationCache.size() + " players");
		} catch (Exception e) {
			log("[Breathless] Failed loading player data");
		}
	}

	private void saveAllPlayers() {
		try {
			java.io.FileWriter w = new java.io.FileWriter(playerDataFile);
			for (var entry : acclimationCache.entrySet()) {
				w.write(entry.getKey() + "=" + entry.getValue() + "\n");
			}
			w.close();
		} catch (Exception e) {
			log("[Breathless] Failed saving player data");
		}
	}

	// Breathtaking math!
		// Return carbon levels
			private int return_CARBON_LEVELS(double input_Y) {
				if (input_Y > 64) {
					return 0;
				}
				input_Y-=64;
				int depth_squared = (int)(input_Y * input_Y);
				int carbon_levels = (int)(0.00925*depth_squared);
				return Math.min(carbon_levels, 100);
			}

			private int return_CARBON_LEVELS(int input_Y) {
				if (input_Y > 64) {
					return 0;
				}
				input_Y-=64;
				int depth_squared = (int)(input_Y * input_Y);
				int carbon_levels = (int)(0.00925*depth_squared);
				return Math.min(carbon_levels, 100);
			}

		// Return Air Quality
			private int return_AIR_QUALITY(double input_Y) {
				if (input_Y < 120) {
					return 100;
				}
				input_Y -= 120;
				int altitude_squared = (int)(input_Y * input_Y);
				int air_quality = 100 - (int)(0.0025 * altitude_squared);
				return Math.max(air_quality, 0);
			}

			private int return_AIR_QUALITY(int input_Y) {
				if (input_Y < 120) {
					return 100;
				}
				input_Y -= 120;
				int altitude_squared = (int)(input_Y * input_Y);
				int air_quality = 100 - (int)(0.0025 * altitude_squared);
				return Math.max(air_quality, 0);
			}
		
		// In range check
		public boolean inRange(int value, int max, int min) {
			return value >= min && value <= max;
		}

		// Shift
		public int shift(int value, int target) {
			if (value > target) { return value-1; }
			else if (value < target) { return value+1; }
			else { return value; }
		}
	
	// Air-checK!
		private void do_AIR_CHECK(MinecraftServer INSTANCE) {
			for (ServerPlayer PLAYER : INSTANCE.getPlayerList().getPlayers()) {
				double y_level = PLAYER.position().y;

				if (y_level >= 120) {
					int air_quality = return_AIR_QUALITY(y_level);

					int acclimation = 1;
					shift(acclimation, air_quality);
					boolean acclimated = inRange(acclimation, air_quality + 3, air_quality - 3);

					var Indifferent_Hypoxia = new MobEffectInstance(MobEffects.WEAKNESS, 20 * secondsDelayTillNextBreath, 0, true, false);
					var Compensatory_Hypoxia_A = new MobEffectInstance(MobEffects.WEAKNESS, 20 * secondsDelayTillNextBreath * 2, 1, true, false); 
					var Compensatory_Hypoxia_B = new MobEffectInstance(MobEffects.MINING_FATIGUE, 20 * secondsDelayTillNextBreath, 0, true, false); 
					var Critical_Hypoxia_A = new MobEffectInstance(MobEffects.WEAKNESS, 20 * secondsDelayTillNextBreath * 3, 2, true, false); 
					var Critical_Hypoxia_B = new MobEffectInstance(MobEffects.MINING_FATIGUE, 20 * secondsDelayTillNextBreath * 2, 1, true, false); 
					var Critical_Hypoxia_C = new MobEffectInstance(MobEffects.SLOWNESS, 20 * secondsDelayTillNextBreath * 1, 0, true, false);
					var Severe_Hypoxia = new MobEffectInstance(MobEffects.INSTANT_DAMAGE, 1, 0, true, false);
					var Lung_Collapse = new MobEffectInstance(MobEffects.INSTANT_DAMAGE, 2, 0, true, false);
				
					if (!PLAYER.hasEffect(MobEffects.WATER_BREATHING) && !PLAYER.hasEffect(MobEffects.CONDUIT_POWER)) {
						if (inRange(air_quality, 100, 90)) {
							// Passkl
						} else if (inRange(air_quality, 89, 80) && !acclimated) {
							// Indifferent Hypoxia. Minor effects
							PLAYER.addEffect(Indifferent_Hypoxia);
						} else if (inRange(air_quality, 79, 70) && !acclimated) {
							// Compensatory Hypoxia. Mild effects.
							PLAYER.addEffect(Compensatory_Hypoxia_A);
							PLAYER.addEffect(Compensatory_Hypoxia_B);
						} else if (inRange(air_quality, 69, 60) && !acclimated) {
							// Critical Hypoxia. Severe effects.
							PLAYER.addEffect(Critical_Hypoxia_A);
							PLAYER.addEffect(Critical_Hypoxia_B);
							PLAYER.addEffect(Critical_Hypoxia_C);
						} else if (inRange(air_quality, 59, 30) && !acclimated) {
							// Severe Hypoxia. Dangerous effects.
							PLAYER.addEffect(Critical_Hypoxia_A);
							PLAYER.addEffect(Critical_Hypoxia_B);
							PLAYER.addEffect(Critical_Hypoxia_C);
							PLAYER.addEffect(Severe_Hypoxia);
						} else if (inRange(acclimation, 29, 0)) {
							// Critical Hypoxia. Severe effects. Doesn't care if you are acclimated or not.
							PLAYER.addEffect(Critical_Hypoxia_A);
							PLAYER.addEffect(Critical_Hypoxia_B);
							PLAYER.addEffect(Critical_Hypoxia_C);
							if (inRange(acclimation, 20, 0)) {
								PLAYER.addEffect(Lung_Collapse);
							}
						} else {
							// Pass
						}
					}

					// Code here for persistent acclimation
				} else if (y_level <= 64) {
					int carbon_levels = return_CARBON_LEVELS(y_level);
					int randomNum = (int)(Math.random() * 101); // 0 to 100

					// No acclimation. Just air stagnation.
					var Mild_Hypercapnia_A = new MobEffectInstance(MobEffects.MINING_FATIGUE, 21 * secondsDelayTillNextBreath, 0, true, false, true);
					var Mild_Hypercapnia_Quick_B = new MobEffectInstance(MobEffects.POISON, 20, 1, true, false, false);
					var Mild_Hypercapnia_Quick_C = new MobEffectInstance(MobEffects.NAUSEA, 20 * secondsDelayTillNextBreath, 1, true, false);

					var Moderate_Hypercapnia_A = new MobEffectInstance(MobEffects.MINING_FATIGUE, 21 * secondsDelayTillNextBreath, 1, true, false, true); 
					var Moderate_Hypercapnia_B = new MobEffectInstance(MobEffects.SLOWNESS, 21 * secondsDelayTillNextBreath, 0, true, false, true); 
					var Moderate_Hypercapnia_Quick_C = new MobEffectInstance(MobEffects.NAUSEA, 20 * secondsDelayTillNextBreath * 2, 1, true, false);

					var Severe_Hypercapnia_A = new MobEffectInstance(MobEffects.MINING_FATIGUE, 21 * secondsDelayTillNextBreath, 2, true, false, true); 
					var Severe_Hypercapnia_B = new MobEffectInstance(MobEffects.SLOWNESS, 21 * secondsDelayTillNextBreath, 1, true, false, true); 

					var Organ_Failure = new MobEffectInstance(MobEffects.WITHER, 999999999 , 1, true, false, true); 

					if (!PLAYER.hasEffect(MobEffects.WATER_BREATHING) && !PLAYER.hasEffect(MobEffects.CONDUIT_POWER)) {
						if (inRange(carbon_levels, 10, 0)) {
							// Pass. Safe
						} else if (inRange(carbon_levels, 20, 11)) {
							PLAYER.addEffect(Mild_Hypercapnia_A);
							if (randomNum >= 66.6) { PLAYER.addEffect(Mild_Hypercapnia_Quick_B); }
							else if (randomNum <= 15.0) { PLAYER.addEffect(Mild_Hypercapnia_Quick_C); PLAYER.drop(true); }
							//log("Mild: " + randomNum);

						} else if (inRange(carbon_levels, 40, 21)) {
							PLAYER.addEffect(Moderate_Hypercapnia_A);
							PLAYER.addEffect(Moderate_Hypercapnia_B);
							if (randomNum >= 33.3) { PLAYER.addEffect(Mild_Hypercapnia_Quick_B); }
							if (randomNum >= 66.6) { PLAYER.addEffect(Moderate_Hypercapnia_Quick_C); PLAYER.drop(true); }
							//log("Moderate: " + randomNum);
							
						} else if (inRange(carbon_levels, 90, 41)) {
							PLAYER.addEffect(Severe_Hypercapnia_A);
							PLAYER.addEffect(Severe_Hypercapnia_B);
							if (randomNum >= 10.0) { PLAYER.addEffect(Mild_Hypercapnia_Quick_B); }
							if (randomNum >= 30.0) { PLAYER.addEffect(Moderate_Hypercapnia_Quick_C); PLAYER.drop(true); }
							//log("Severe: " + randomNum);

						} else if (inRange(carbon_levels, 100, 91)) {
							PLAYER.addEffect(Severe_Hypercapnia_A);
							PLAYER.addEffect(Severe_Hypercapnia_B);

							// 13 seconds to die from organ failure assuming you started with full HP. Only milk can save you now.
							PLAYER.addEffect(Organ_Failure);
						} else {
							// Pass.
						}
					}

					// No persistence required
				}
			}
		}

	@Override
	public void onInitialize() {
		LOGGER.info("[Breathless/Main] Alive and breathing!");

		Path configFolder = Paths.get(System.getProperty("user.dir"), "config", "breathless");
	    Path playerDataFilePath = configFolder.resolve("playerData.txt");

		try {
			if (!Files.exists(configFolder)) {
				log("[Breathless] Creating config folder...");
				Files.createDirectories(configFolder);
			}

			if (!Files.exists(playerDataFilePath)) {
				Files.createFile(playerDataFilePath);
				log("[Breathless] Created playerData.txt");
			}
		} catch (Exception e) {
			log("[Breathless] Failed to create config or data file: " + e);
		}

		playerDataFile = playerDataFilePath.toString();
		loadAllPlayers();
			
		ServerTickEvents.START_SERVER_TICK.register((LISTENER) -> {
			ticksTillNextBreath++;

			if (ticksTillNextBreath >= secondsDelayTillNextBreath * 20) {
				ticksTillNextBreath = 0;
				do_AIR_CHECK(LISTENER);
			}
		});

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(
				literal("air")
					.then(literal("quality").executes(context -> {
						CommandSourceStack source = context.getSource();
						ServerPlayer player = source.getPlayer();

						int y_level = (int)player.position().y();
						int air_quality = return_AIR_QUALITY(y_level);
						int acclimation = getPlayerInt(player);
						int carbon_levels = return_CARBON_LEVELS(y_level);

						if (y_level >= 120) {
							source.sendSuccess(() -> Component.literal("[ Altitude Sickness Risk! ]"), false);
							if (!inRange(acclimation, air_quality+3, air_quality-3)) {
								source.sendSuccess(() -> Component.literal("[ Body Acclimation: 100%  ]"), false);
							} else if (!inRange(acclimation, air_quality+6, air_quality-6)) {
								source.sendSuccess(() -> Component.literal("[ Body Acclimation: ~67%  ]"), false);
							} else if (!inRange(acclimation, air_quality+9, air_quality-9)) {
								source.sendSuccess(() -> Component.literal("[ Body Acclimation: ~36%  ]"), false);
							} else if (!inRange(acclimation, air_quality+9, air_quality-9)) {
								source.sendSuccess(() -> Component.literal("[ Body Acclimation: ~13%  ]"), false);
							} else if (!inRange(acclimation, air_quality+6, air_quality-6)) {
								source.sendSuccess(() -> Component.literal("[ Body Acclimation: None! ]"), false);
							} else {
								source.sendSuccess(() -> Component.literal("[ There was an error fet. ]"), false);
							}	

						} else if (y_level <= 63) {
							source.sendSuccess(() -> Component.literal("[ Elevated Carbon Dioxide Presence Detected ]"), false);
							if (inRange(carbon_levels, 10, 0)) { //[ Elevated Carbon Dioxide Presence Detected ]
								source.sendSuccess(() -> Component.literal("[ Hypercapnia risk: Mild ]"), false);
							} else if (inRange(carbon_levels, 20, 11)) {
								source.sendSuccess(() -> Component.literal("[ Hypercapnia risk: Moderate ]"), false);
							} else if (inRange(carbon_levels, 40, 21)) {
								source.sendSuccess(() -> Component.literal("[ Hypercapnia risk: Severe ]"), false);
							} else if (inRange(carbon_levels, 80, 41)) {
								source.sendSuccess(() -> Component.literal("[ Hypercapnia risk: Certain and fatal ]"), false);
							} else {
								source.sendSuccess(() -> Component.literal("[ There was an error fet. ]"), false);
							}	
						}

						return 1;
					}))
			);
		});
	}
}