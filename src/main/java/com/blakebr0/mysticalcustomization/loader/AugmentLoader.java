package com.blakebr0.mysticalcustomization.loader;

import com.blakebr0.mysticalagriculture.api.registry.IAugmentRegistry;
import com.blakebr0.mysticalagriculture.api.tinkering.IAugment;
import com.blakebr0.mysticalcustomization.MysticalCustomization;
import com.blakebr0.mysticalcustomization.modify.AugmentModifier;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;

public final class AugmentLoader {
    private static final Logger LOGGER = LogManager.getLogger(MysticalCustomization.NAME);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    public static void onPostRegisterAugments(IAugmentRegistry registry) {
        File dir = FMLPaths.CONFIGDIR.get().resolve("mysticalcustomization/").toFile();
        if (!dir.exists() && dir.mkdirs()) {
            LOGGER.info("Created /config/mysticalcustomization/ directory");
        }

        File file = FMLPaths.CONFIGDIR.get().resolve("mysticalcustomization/configure-augments.json").toFile();
        if (file.exists() && file.isFile()) {
            JsonObject json;
            FileReader reader = null;

            try {
                JsonParser parser = new JsonParser();
                reader = new FileReader(file);
                json = parser.parse(reader).getAsJsonObject();

                json.entrySet().forEach(entry -> {
                    String id = entry.getKey();
                    JsonObject changes = entry.getValue().getAsJsonObject();
                    IAugment augment = registry.getAugmentById(new ResourceLocation(id));

                    if (augment == null) {
                        String error = String.format("Invalid augment id provided: %s", id);
                        throw new JsonParseException(error);
                    }

                    AugmentModifier.modify(augment, changes);
                });

                reader.close();
            } catch (Exception e) {
                LOGGER.error("An error occurred while reading configure-augment.json", e);
            } finally {
                IOUtils.closeQuietly(reader);
            }
        } else {
            try (Writer writer = new FileWriter(file)) {
                JsonObject object = new JsonObject();
                GSON.toJson(object, writer);
            } catch (IOException e) {
                LOGGER.error("An error occurred while creating configure-augment.json", e);
            }
        }
    }
}
