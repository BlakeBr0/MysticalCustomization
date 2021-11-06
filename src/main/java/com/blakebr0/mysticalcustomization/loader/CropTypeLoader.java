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
import net.minecraft.item.Item;
import net.minecraft.item.Items;
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

public final class CropTypeLoader {
    private static final Logger LOGGER = LogManager.getLogger(MysticalCustomization.NAME);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    public static final Map<CropType, ResourceLocation> CRAFTING_SEED_MAP = new HashMap<>();

    public static void onRegisterCrops() {
        File dir = FMLPaths.CONFIGDIR.get().resolve("mysticalcustomization/types/").toFile();
        if (!dir.exists() && dir.mkdirs()) {
            LOGGER.info("Created /config/mysticalcustomization/types/ directory");
        }

        File[] files = dir.listFiles((FileFilter) FileFilterUtils.suffixFileFilter(".json"));
        if (files == null)
            return;

        for (File file : files) {
            JsonObject json;
            InputStreamReader reader = null;
            ResourceLocation id = null;

            try {
                JsonParser parser = new JsonParser();
                reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8);
                json = parser.parse(reader).getAsJsonObject();
                String name = file.getName().replace(".json", "");
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
        File dir = FMLPaths.CONFIGDIR.get().resolve("mysticalcustomization/").toFile();
        if (!dir.exists() && dir.mkdirs()) {
            LOGGER.info("Created /config/mysticalcustomization/ directory");
        }

        File file = FMLPaths.CONFIGDIR.get().resolve("mysticalcustomization/configure-types.json").toFile();
        if (file.exists() && file.isFile()) {
            JsonObject json;
            FileReader reader = null;

            try {
                JsonParser parser = new JsonParser();
                reader = new FileReader(file);
                json = parser.parse(reader).getAsJsonObject();

                json.entrySet().forEach(entry -> {
                    String name = entry.getKey();
                    JsonObject changes = entry.getValue().getAsJsonObject();
                    CropType type = MysticalAgricultureAPI.getCropTypeByName(name);

                    if (type == null) {
                        String error = String.format("Invalid crop type id provided: %s", name);
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
            try (Writer writer = new FileWriter(file)) {
                JsonObject object = new JsonObject();
                GSON.toJson(object, writer);
            } catch (IOException e) {
                LOGGER.error("An error occurred while creating configure-types.json", e);
            }
        }
    }

    public static void onCommonSetup() {
        CRAFTING_SEED_MAP.forEach((type, item) -> {
            Item craftingSeed = ForgeRegistries.ITEMS.getValue(item);
            if (craftingSeed != Items.AIR) {
                type.setCraftingSeed(() -> craftingSeed);
            } else {
                throw new JsonSyntaxException("Invalid crafting seed item provided");
            }
        });
    }
}
