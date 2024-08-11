package com.blakebr0.mysticalcustomization.loader;

import com.blakebr0.mysticalagriculture.api.crop.CropType;
import com.blakebr0.mysticalagriculture.api.registry.ICropRegistry;
import com.blakebr0.mysticalcustomization.MysticalCustomization;
import com.blakebr0.mysticalcustomization.create.CropTypeCreator;
import com.blakebr0.mysticalcustomization.modify.CropTypeModifier;
import com.blakebr0.mysticalcustomization.util.ErrorManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.neoforged.fml.loading.FMLPaths;
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
    private static final String CATEGORY = "Crop Type";

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
            InputStreamReader reader = null;
            ResourceLocation id = null;
            CropType type = null;

            try {
                reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8);
                var json = JsonParser.parseReader(reader).getAsJsonObject();
                var name = file.getName().replace(".json", "");
                id = MysticalCustomization.resource(name);

                try {
                    type = CropTypeCreator.create(name, json);
                } catch (JsonSyntaxException | ResourceLocationException e) {
                    ErrorManager.INSTANCE.addError(CATEGORY, "Creating %s: %s".formatted(id, e.getMessage()));
                }

                reader.close();
            } catch (Exception e) {
                ErrorManager.INSTANCE.addFatalError(CATEGORY, "An error occurred creating crop type with id %s.".formatted(id), e);
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
            FileReader reader = null;

            try {
                reader = new FileReader(file);
                var json = JsonParser.parseReader(reader).getAsJsonObject();

                for (var entry : json.entrySet()) {
                    var id = entry.getKey();
                    var changes = entry.getValue().getAsJsonObject();
                    var type = registry.getTypeById(ResourceLocation.tryParse(id));

                    try {
                        if (type == null) {
                            throw new JsonSyntaxException("Unknown crop type id: %s".formatted(id));
                        }

                        CropTypeModifier.modify(type, changes);
                    } catch (JsonSyntaxException | ResourceLocationException e) {
                        ErrorManager.INSTANCE.addError(CATEGORY, "Modifying %s: %s".formatted(id, e.getMessage()));
                    }
                }

                reader.close();
            } catch (Exception e) {
                ErrorManager.INSTANCE.addFatalError(CATEGORY, "An error occurred while reading configure-types.json.", e);
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
            var craftingSeed = BuiltInRegistries.ITEM.get(item);
            if (craftingSeed != Items.AIR) {
                type.setCraftingSeed(() -> craftingSeed);
            } else {
                ErrorManager.INSTANCE.addError(CATEGORY, "Modifying %s: %s".formatted(type, "Invalid crafting seed item: %s".formatted(item)));
            }
        });
    }
}
