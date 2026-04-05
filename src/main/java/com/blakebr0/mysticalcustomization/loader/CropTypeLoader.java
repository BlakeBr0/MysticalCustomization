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
import net.minecraft.IdentifierException;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Items;
import net.neoforged.fml.loading.FMLPaths;
import org.apache.commons.io.IOUtils;

import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public final class CropTypeLoader {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final String CATEGORY = "Crop Type";

    public static final Map<CropType, Identifier> CRAFTING_SEED_MAP = new HashMap<>();

    public static void onRegisterCrops(ICropRegistry registry) {
        var dir = FMLPaths.CONFIGDIR.get().resolve("mysticalcustomization/types/").toFile();
        if (!dir.exists() && dir.mkdirs()) {
            MysticalCustomization.LOGGER.info("Created /config/mysticalcustomization/types/ directory");
        }

        try (var paths = Files.walk(dir.toPath())) {
            var files = paths
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().toLowerCase().endsWith(".json"))
                    .map(Path::toFile)
                    .toList();

            for (var file : files) {
                InputStreamReader reader = null;
                Identifier id = null;
                CropType type = null;

                try {
                    reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8);
                    var json = JsonParser.parseReader(reader).getAsJsonObject();
                    var name = file.getName().replace(".json", "");
                    id = MysticalCustomization.resource(name);

                    try {
                        type = CropTypeCreator.create(name, json);
                    } catch (JsonSyntaxException | IdentifierException e) {
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
        } catch (Exception e) {
            ErrorManager.INSTANCE.addFatalError(CATEGORY, "An error occurred while processing crop type files.", e);
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
                    var type = registry.getTypeById(Identifier.tryParse(id));

                    try {
                        if (type == null) {
                            throw new JsonSyntaxException("Unknown crop type id: %s".formatted(id));
                        }

                        CropTypeModifier.modify(type, changes);
                    } catch (JsonSyntaxException | IdentifierException e) {
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
            var craftingSeed = BuiltInRegistries.ITEM.getValue(item);
            if (craftingSeed != Items.AIR) {
                type.setCraftingSeed(() -> craftingSeed);
            } else {
                ErrorManager.INSTANCE.addError(CATEGORY, "Modifying %s: %s".formatted(type, "Invalid crafting seed item: %s".formatted(item)));
            }
        });
    }
}
