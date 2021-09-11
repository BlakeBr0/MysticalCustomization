package com.blakebr0.mysticalcustomization.loader;

import com.blakebr0.mysticalagriculture.api.MysticalAgricultureAPI;
import com.blakebr0.mysticalagriculture.api.crop.CropType;
import com.blakebr0.mysticalcustomization.MysticalCustomization;
import com.blakebr0.mysticalcustomization.create.CropTypeCreator;
import com.blakebr0.mysticalcustomization.modify.CropTypeModifier;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileFilter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public final class CropTypeLoader {
    private static final Logger LOGGER = LogManager.getLogger(MysticalCustomization.NAME);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    public static final Map<CropType, ResourceLocation> CRAFTING_SEED_MAP = new HashMap<>();

    public static void onRegisterCrops() {
        var dir = FMLPaths.CONFIGDIR.get().resolve("mysticalcustomization/types/").toFile();
        if (!dir.exists() && dir.mkdirs()) {
            LOGGER.info("Created /config/mysticalcustomization/types/ directory");
        }

        var files = dir.listFiles((FileFilter) FileFilterUtils.suffixFileFilter(".json"));
        if (files == null)
            return;

        for (var file : files) {
            JsonObject json;
            FileReader reader = null;
            ResourceLocation id = null;

            try {
                var parser = new JsonParser();
                reader = new FileReader(file);
                json = parser.parse(reader).getAsJsonObject();
                var name = file.getName().replace(".json", "");

                CropTypeCreator.create(name, json);

                reader.close();
            } catch (Exception e) {
                LOGGER.error("An error occurred while creating crop type with id {}", id, e);
            } finally {
                IOUtils.closeQuietly(reader);
            }
        }
    }

    public static void onPostRegisterCrops() {
        var dir = FMLPaths.CONFIGDIR.get().resolve("mysticalcustomization/").toFile();
        if (!dir.exists() && dir.mkdirs()) {
            LOGGER.info("Created /config/mysticalcustomization/ directory");
        }

        var file = FMLPaths.CONFIGDIR.get().resolve("mysticalcustomization/configure-types.json").toFile();
        if (file.exists() && file.isFile()) {
            JsonObject json;
            FileReader reader = null;

            try {
                var parser = new JsonParser();
                reader = new FileReader(file);
                json = parser.parse(reader).getAsJsonObject();

                json.entrySet().forEach(entry -> {
                    var name = entry.getKey();
                    var changes = entry.getValue().getAsJsonObject();
                    var type = MysticalAgricultureAPI.getCropTypeByName(name);

                    if (type == null) {
                        var error = String.format("Invalid crop type id provided: %s", name);
                        throw new JsonParseException(error);
                    }

                    CropTypeModifier.modify(type, changes);
                });

                reader.close();
            } catch (Exception e) {
                LOGGER.error("An error occurred while reading configure-types.json", e);
            } finally {
                IOUtils.closeQuietly(reader);
            }
        } else {
            try (var writer = new FileWriter(file)) {
                var object = new JsonObject();
                GSON.toJson(object, writer);
            } catch (IOException e) {
                LOGGER.error("An error occurred while creating configure-types.json", e);
            }
        }
    }

    public static void onCommonSetup() {
        CRAFTING_SEED_MAP.forEach((type, item) -> {
            var craftingSeed = ForgeRegistries.ITEMS.getValue(item);
            if (craftingSeed != Items.AIR) {
                type.setCraftingSeed(() -> craftingSeed);
            } else {
                throw new JsonSyntaxException("Invalid crafting seed item provided");
            }
        });
    }
}
