package com.blakebr0.mysticalcustomization.loader;

import com.blakebr0.mysticalagriculture.api.crop.Crop;
import com.blakebr0.mysticalagriculture.api.registry.ICropRegistry;
import com.blakebr0.mysticalcustomization.MysticalCustomization;
import com.blakebr0.mysticalcustomization.create.CropCreator;
import com.blakebr0.mysticalcustomization.modify.CropModifier;
import com.blakebr0.mysticalcustomization.util.ErrorManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import net.minecraft.IdentifierException;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Blocks;
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

public final class CropLoader {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final String CATEGORY = "Crops";

    public static final Map<Crop, Identifier> CRUX_MAP = new HashMap<>();
    public static final Map<Crop, Identifier> CROP_TIER_MAP = new HashMap<>();
    public static final Map<Crop, Identifier> CROP_TYPE_MAP = new HashMap<>();

    public static void onRegisterCrops(ICropRegistry registry) {
        var dir = FMLPaths.CONFIGDIR.get().resolve("mysticalcustomization/crops/").toFile();
        if (!dir.exists() && dir.mkdirs()) {
            MysticalCustomization.LOGGER.info("Created /config/mysticalcustomization/crops/ directory");
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
                Crop crop = null;

                try {
                    reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8);
                    var json = JsonParser.parseReader(reader).getAsJsonObject();
                    var name = file.getName().replace(".json", "");
                    id = MysticalCustomization.resource(name);

                    try {
                        crop = CropCreator.create(id, json);
                    } catch (JsonSyntaxException | IdentifierException e) {
                        ErrorManager.INSTANCE.addError(CATEGORY, e.getMessage());
                    }

                    reader.close();
                } catch (Exception e) {
                    ErrorManager.INSTANCE.addFatalError(CATEGORY, "An error occurred creating crop with id %s.".formatted(id), e);
                } finally {
                    IOUtils.closeQuietly(reader);
                }

                if (crop != null)
                    registry.register(crop);
            }
        } catch (Exception e) {
            ErrorManager.INSTANCE.addFatalError(CATEGORY, "An error occurred while processing crop files.", e);
        }
    }

    public static void onPostRegisterCrops(ICropRegistry registry) {
        CROP_TIER_MAP.forEach((crop, id) -> {
            var tier = registry.getTierById(id);
            if (tier == null) {
                ErrorManager.INSTANCE.addError(CATEGORY, "Creating %s: %s".formatted(crop.getId(), "Invalid crop tier: %s".formatted(id)));
            } else {
                crop.setTier(tier);
            }
        });

        CROP_TYPE_MAP.forEach((crop, id) -> {
            var type = registry.getTypeById(id);
            if (type == null) {
                ErrorManager.INSTANCE.addError(CATEGORY, "Creating %s: %s".formatted(crop.getId(), "Invalid crop type: %s".formatted(id)));
            } else {
                crop.setType(type);
            }
        });

        var dir = FMLPaths.CONFIGDIR.get().resolve("mysticalcustomization/").toFile();
        if (!dir.exists() && dir.mkdirs()) {
            MysticalCustomization.LOGGER.info("Created /config/mysticalcustomization/ directory");
        }

        var file = FMLPaths.CONFIGDIR.get().resolve("mysticalcustomization/configure-crops.json").toFile();
        if (file.exists() && file.isFile()) {
            FileReader reader = null;

            try {
                reader = new FileReader(file);
                var json = JsonParser.parseReader(reader).getAsJsonObject();

                for (var entry : json.entrySet()) {
                    var id = entry.getKey();
                    var changes = entry.getValue().getAsJsonObject();
                    var crop = registry.getCropById(Identifier.tryParse(id));

                    try {
                        if (crop == null) {
                            throw new JsonSyntaxException("Unknown crop id: %s".formatted(id));
                        }

                        CropModifier.modify(crop, changes);
                    } catch (JsonSyntaxException | IdentifierException e) {
                        ErrorManager.INSTANCE.addError(CATEGORY, "Modifying %s: %s".formatted(id, e.getMessage()));
                    }
                }

                reader.close();
            } catch (Exception e) {
                ErrorManager.INSTANCE.addFatalError(CATEGORY, "An error occurred while reading configure-crops.json.", e);
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
            if (crux == null) {
                crop.setCruxBlock(null);
            } else {
                var block = BuiltInRegistries.BLOCK.getValue(crux);
                if (block != Blocks.AIR) {
                    crop.setCruxBlock(() -> block);
                } else {
                    ErrorManager.INSTANCE.addError(CATEGORY, "Modifying %s: %s".formatted(crop.getId(), "Invalid crux block: %s".formatted(crux)));
                }
            }
        });
    }
}
