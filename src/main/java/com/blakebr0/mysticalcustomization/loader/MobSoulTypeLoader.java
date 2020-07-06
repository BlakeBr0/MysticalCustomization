package com.blakebr0.mysticalcustomization.loader;

import com.blakebr0.mysticalagriculture.api.registry.IMobSoulTypeRegistry;
import com.blakebr0.mysticalagriculture.api.soul.IMobSoulType;
import com.blakebr0.mysticalcustomization.MysticalCustomization;
import com.blakebr0.mysticalcustomization.create.MobSoulTypeCreator;
import com.blakebr0.mysticalcustomization.modify.MobSoulTypeModifier;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

public final class MobSoulTypeLoader {
    private static final Logger LOGGER = LogManager.getLogger(MysticalCustomization.NAME);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    public static void onRegisterMobSoulTypes(IMobSoulTypeRegistry registry) {
        File dir = FMLPaths.CONFIGDIR.get().resolve("mysticalcustomization/mobsoultypes/").toFile();
        if (!dir.exists() && dir.mkdirs()) {
            LOGGER.info("Created /config/mysticalcustomization/mobsoultypes/ directory");
        }

        File[] files = dir.listFiles((FileFilter) FileFilterUtils.suffixFileFilter(".json"));
        if (files == null)
            return;

        for (File file : files) {
            JsonObject json;
            FileReader reader = null;
            ResourceLocation id = null;
            IMobSoulType type = null;

            try {
                JsonParser parser = new JsonParser();
                reader = new FileReader(file);
                json = parser.parse(reader).getAsJsonObject();
                String name = file.getName().replace(".json", "");
                id = new ResourceLocation(MysticalCustomization.MOD_ID, name);
                type = MobSoulTypeCreator.create(id, json);

                reader.close();
            } catch (Exception e) {
                LOGGER.error("An error occurred while creating mob soul type with id {}", id, e);
            } finally {
                IOUtils.closeQuietly(reader);
            }

            if (type != null)
                registry.register(type);
        }
    }

    public static void onPostRegisterMobSoulTypes(IMobSoulTypeRegistry registry) {
        File dir = FMLPaths.CONFIGDIR.get().resolve("mysticalcustomization/").toFile();
        if (!dir.exists() && dir.mkdirs()) {
            LOGGER.info("Created /config/mysticalcustomization/ directory");
        }

        File file = FMLPaths.CONFIGDIR.get().resolve("mysticalcustomization/configure-mobsoultypes.json").toFile();
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
                    IMobSoulType type = registry.getMobSoulTypeById(new ResourceLocation(id));

                    MobSoulTypeModifier.modify(type, changes);
                });

                reader.close();
            } catch (Exception e) {
                LOGGER.error("An error occurred while reading configure-mobsoultypes.json", e);
            } finally {
                IOUtils.closeQuietly(reader);
            }
        } else {
            try (Writer writer = new FileWriter(file)) {
                JsonObject object = new JsonObject();
                GSON.toJson(object, writer);
            } catch (IOException e) {
                LOGGER.error("An error occurred while creating configure-mobsoultypes.json", e);
            }
        }
    }
}
