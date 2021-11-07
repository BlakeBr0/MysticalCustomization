package com.blakebr0.mysticalcustomization.loader;

import com.blakebr0.mysticalagriculture.api.crop.Crop;
import com.blakebr0.mysticalagriculture.api.registry.ICropRegistry;
import com.blakebr0.mysticalcustomization.MysticalCustomization;
import com.blakebr0.mysticalcustomization.create.CropCreator;
import com.blakebr0.mysticalcustomization.modify.CropModifier;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;

import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public final class CropLoader {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    public static final Map<Crop, ResourceLocation> CRUX_MAP = new HashMap<>();

    public static void onRegisterCrops(ICropRegistry registry) {
        var dir = FMLPaths.CONFIGDIR.get().resolve("mysticalcustomization/crops/").toFile();
        if (!dir.exists() && dir.mkdirs()) {
            MysticalCustomization.LOGGER.info("Created /config/mysticalcustomization/crops/ directory");
        }

        var files = dir.listFiles((FileFilter) FileFilterUtils.suffixFileFilter(".json"));
        if (files == null)
            return;

        for (var file : files) {
            JsonObject json;
            InputStreamReader reader = null;
            ResourceLocation id = null;
            Crop crop = null;

            try {
                var parser = new JsonParser();
                reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8);
                json = parser.parse(reader).getAsJsonObject();
                var name = file.getName().replace(".json", "");
                id = new ResourceLocation(MysticalCustomization.MOD_ID, name);

                crop = CropCreator.create(id, json);

                reader.close();
            } catch (Exception e) {
                MysticalCustomization.LOGGER.error("An error occurred while creating crop with id {}", id, e);
            } finally {
                IOUtils.closeQuietly(reader);
            }

            if (crop != null)
                registry.register(crop);
        }
    }

    public static void onPostRegisterCrops(ICropRegistry registry) {
        var dir = FMLPaths.CONFIGDIR.get().resolve("mysticalcustomization/").toFile();
        if (!dir.exists() && dir.mkdirs()) {
            MysticalCustomization.LOGGER.info("Created /config/mysticalcustomization/ directory");
        }

        var file = FMLPaths.CONFIGDIR.get().resolve("mysticalcustomization/configure-crops.json").toFile();
        if (file.exists() && file.isFile()) {
            JsonObject json;
            FileReader reader = null;

            try {
                var parser = new JsonParser();
                reader = new FileReader(file);
                json = parser.parse(reader).getAsJsonObject();

                json.entrySet().forEach(entry -> {
                    var id = entry.getKey();
                    var changes = entry.getValue().getAsJsonObject();
                    var crop = registry.getCropById(new ResourceLocation(id));

                    if (crop == null) {
                        var error = String.format("Invalid crop id provided: %s", id);
                        throw new JsonParseException(error);
                    }

                    CropModifier.modify(crop, changes);
                });

                reader.close();
            } catch (Exception e) {
                MysticalCustomization.LOGGER.error("An error occurred while reading configure-crops.json", e);
            } finally {
                IOUtils.closeQuietly(reader);
            }
        } else {
            try (var writer = new FileWriter(file)) {
                var object = new JsonObject();
                GSON.toJson(object, writer);
            } catch (IOException e) {
                MysticalCustomization.LOGGER.error("An error occurred while creating configure-crops.json", e);
            }
        }
    }

    public static void onCommonSetup() {
        CRUX_MAP.forEach((crop, crux) -> {
            var block = ForgeRegistries.BLOCKS.getValue(crux);
            if (block != Blocks.AIR) {
                crop.setCruxBlock(() -> block);
            } else {
                MysticalCustomization.LOGGER.error("Could not find crux for crop {}", crop.getId());
            }
        });
    }
}
