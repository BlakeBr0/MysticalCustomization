package com.blakebr0.mysticalcustomization.loader;

import com.blakebr0.mysticalagriculture.api.crop.CropType;
import com.blakebr0.mysticalagriculture.api.registry.ICropRegistry;
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

import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public final class CropTypeLoader {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    public static final Map<CropType, ResourceLocation> CRAFTING_SEED_MAP = new HashMap<>();

    public static void onRegisterCrops(ICropRegistry registry) {
        var dir = FMLPaths.CONFIGDIR.get().resolve("mysticalcustomization/types/").toFile();
        if (!dir.exists() && dir.mkdirs()) {
            MysticalCustomization.LOGGER.info("Created /config/mysticalcustomization/types/ directory");
        }

        var files = dir.listFiles((FileFilter) FileFilterUtils.suffixFileFilter(".json"));
        if (files == null)
            return;

        for (var file : files) {
            JsonObject json;
            InputStreamReader reader = null;
            ResourceLocation id = null;
            CropType type = null;

            try {
                var parser = new JsonParser();
                reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8);
                json = parser.parse(reader).getAsJsonObject();
                var name = file.getName().replace(".json", "");
                id = new ResourceLocation(MysticalCustomization.MOD_ID, name);

                type = CropTypeCreator.create(name, json);

                reader.close();
            } catch (Exception e) {
                MysticalCustomization.LOGGER.error("An error occurred while creating crop type with id {}", id, e);
            } finally {
                IOUtils.closeQuietly(reader);
            }

            if (type != null)
                registry.registerType(type);
        }
    }

    public static void onPostRegisterCrops(ICropRegistry registry) {
        var dir = FMLPaths.CONFIGDIR.get().resolve("mysticalcustomization/").toFile();
        if (!dir.exists() && dir.mkdirs()) {
            MysticalCustomization.LOGGER.info("Created /config/mysticalcustomization/ directory");
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
                    var type = registry.getTypeById(new ResourceLocation(name));

                    if (type == null) {
                        var error = String.format("Invalid crop type id provided: %s", name);
                        throw new JsonParseException(error);
                    }

                    CropTypeModifier.modify(type, changes);
                });

                reader.close();
            } catch (Exception e) {
                MysticalCustomization.LOGGER.error("An error occurred while reading configure-types.json", e);
            } finally {
                IOUtils.closeQuietly(reader);
            }
        } else {
            try (var writer = new FileWriter(file)) {
                var object = new JsonObject();
                GSON.toJson(object, writer);
            } catch (IOException e) {
                MysticalCustomization.LOGGER.error("An error occurred while creating configure-types.json", e);
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
