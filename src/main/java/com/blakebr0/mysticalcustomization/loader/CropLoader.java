package com.blakebr0.mysticalcustomization.loader;

import com.blakebr0.mysticalagriculture.api.crop.ICrop;
import com.blakebr0.mysticalagriculture.api.registry.ICropRegistry;
import com.blakebr0.mysticalcustomization.MysticalCustomization;
import com.blakebr0.mysticalcustomization.create.CropCreator;
import com.blakebr0.mysticalcustomization.modify.CropModifier;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public final class CropLoader {
    private static final Logger LOGGER = LogManager.getLogger(MysticalCustomization.NAME);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    public static final Map<ICrop, ResourceLocation> CRUX_MAP = new HashMap<>();

    public static void onRegisterCrops(ICropRegistry registry) {
        File dir = FMLPaths.CONFIGDIR.get().resolve("mysticalcustomization/crops/").toFile();
        if (!dir.exists() && dir.mkdirs()) {
            LOGGER.info("Created /config/mysticalcustomization/crops/ directory");
        }

        File[] files = dir.listFiles((FileFilter) FileFilterUtils.suffixFileFilter(".json"));
        if (files == null)
            return;

        for (File file : files) {
            JsonObject json;
            InputStreamReader reader = null;
            ResourceLocation id = null;
            ICrop crop = null;

            try {
                JsonParser parser = new JsonParser();
                reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8);
                json = parser.parse(reader).getAsJsonObject();
                String name = file.getName().replace(".json", "");
                id = new ResourceLocation(MysticalCustomization.MOD_ID, name);
                crop = CropCreator.create(id, json);

                reader.close();
            } catch (Exception e) {
                LOGGER.error("An error occurred while creating crop with id {}", id, e);
            } finally {
                IOUtils.closeQuietly(reader);
            }

            if (crop != null)
                registry.register(crop);
        }
    }

    public static void onPostRegisterCrops(ICropRegistry registry) {
        File dir = FMLPaths.CONFIGDIR.get().resolve("mysticalcustomization/").toFile();
        if (!dir.exists() && dir.mkdirs()) {
            LOGGER.info("Created /config/mysticalcustomization/ directory");
        }

        File file = FMLPaths.CONFIGDIR.get().resolve("mysticalcustomization/configure-crops.json").toFile();
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
                    ICrop crop = registry.getCropById(new ResourceLocation(id));

                    if (crop == null) {
                        String error = String.format("Invalid crop id provided: %s", id);
                        throw new JsonParseException(error);
                    }

                    CropModifier.modify(crop, changes);
                });

                reader.close();
            } catch (Exception e) {
                LOGGER.error("An error occurred while reading configure-crops.json", e);
            } finally {
                IOUtils.closeQuietly(reader);
            }
        } else {
            try (Writer writer = new FileWriter(file)) {
                JsonObject object = new JsonObject();
                GSON.toJson(object, writer);
            } catch (IOException e) {
                LOGGER.error("An error occurred while creating configure-crops.json", e);
            }
        }
    }

    public static void onCommonSetup() {
        CRUX_MAP.forEach((crop, crux) -> {
            Block block = ForgeRegistries.BLOCKS.getValue(crux);
            if (block != Blocks.AIR) {
                crop.setCrux(() -> block);
            } else {
                LOGGER.error("Could not find crux for crop {}", crop.getId());
            }
        });
    }
}
