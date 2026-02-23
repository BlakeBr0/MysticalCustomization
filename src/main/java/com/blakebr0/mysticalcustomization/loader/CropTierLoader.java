package com.blakebr0.mysticalcustomization.loader;

import com.blakebr0.mysticalagriculture.api.crop.CropTier;
import com.blakebr0.mysticalagriculture.api.registry.ICropRegistry;
import com.blakebr0.mysticalcustomization.MysticalCustomization;
import com.blakebr0.mysticalcustomization.create.CropTierCreator;
import com.blakebr0.mysticalcustomization.modify.CropTierModifier;
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
import net.minecraft.world.level.block.FarmBlock;
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

public final class CropTierLoader {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final String CATEGORY = "Crop Tier";

    public static final Map<CropTier, ResourceLocation> FARMLAND_MAP = new HashMap<>();
    public static final Map<CropTier, ResourceLocation> ESSENCE_MAP = new HashMap<>();

    public static void onRegisterCrops(ICropRegistry registry) {
        var dir = FMLPaths.CONFIGDIR.get().resolve("mysticalcustomization/tiers/").toFile();
        if (!dir.exists() && dir.mkdirs()) {
            MysticalCustomization.LOGGER.info("Created /config/mysticalcustomization/tiers/ directory");
        }

        try (var paths = Files.walk(dir.toPath())) {
            var files = paths
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().toLowerCase().endsWith(".json"))
                    .map(Path::toFile)
                    .toList();

            for (var file : files) {
                InputStreamReader reader = null;
                ResourceLocation id = null;
                CropTier tier = null;

                try {
                    reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8);
                    var json = JsonParser.parseReader(reader).getAsJsonObject();
                    var name = file.getName().replace(".json", "");
                    id = MysticalCustomization.resource(name);

                    try {
                        tier = CropTierCreator.create(id, json);
                    } catch (JsonSyntaxException | ResourceLocationException e) {
                        ErrorManager.INSTANCE.addError(CATEGORY, "Creating %s: %s".formatted(id, e.getMessage()));
                    }

                    reader.close();
                } catch (Exception e) {
                    ErrorManager.INSTANCE.addFatalError(CATEGORY, "An error occurred creating crop tier with id %s.".formatted(id), e);
                } finally {
                    IOUtils.closeQuietly(reader);
                }

                if (tier != null)
                    registry.registerTier(tier);
            }
        } catch (Exception e) {
            ErrorManager.INSTANCE.addFatalError(CATEGORY, "An error occurred while processing crop tier files.", e);
        }
    }

    public static void onPostRegisterCrops(ICropRegistry registry) {
        var dir = FMLPaths.CONFIGDIR.get().resolve("mysticalcustomization/").toFile();
        if (!dir.exists() && dir.mkdirs()) {
            MysticalCustomization.LOGGER.info("Created /config/mysticalcustomization/ directory");
        }

        var file = FMLPaths.CONFIGDIR.get().resolve("mysticalcustomization/configure-tiers.json").toFile();
        if (file.exists() && file.isFile()) {
            FileReader reader = null;

            try {
                reader = new FileReader(file);
                var json = JsonParser.parseReader(reader).getAsJsonObject();

                for (var entry : json.entrySet()) {
                    var id = entry.getKey();
                    var changes = entry.getValue().getAsJsonObject();
                    var tier = registry.getTierById(ResourceLocation.tryParse(id));

                    try {
                        if (tier == null) {
                            throw new JsonSyntaxException("Unknown crop tier id: %s".formatted(id));
                        }

                        CropTierModifier.modify(tier, changes);
                    } catch (JsonSyntaxException | ResourceLocationException e) {
                        ErrorManager.INSTANCE.addError(CATEGORY, "Modifying %s: %s".formatted(id, e.getMessage()));
                    }
                }

                reader.close();
            } catch (Exception e) {
                ErrorManager.INSTANCE.addFatalError(CATEGORY, "An error occurred while reading configure-tiers.json.", e);
            } finally {
                IOUtils.closeQuietly(reader);
            }
        } else {
            try (var writer = new FileWriter(file)) {
                var object = new JsonObject();
                GSON.toJson(object, writer);
            } catch (IOException e) {
                MysticalCustomization.LOGGER.error("An error occurred while creating configure-tiers.json", e);
            }
        }
    }

    public static void onCommonSetup() {
        FARMLAND_MAP.forEach((tier, block) -> {
            var farmland = BuiltInRegistries.BLOCK.get(block);
            if (farmland instanceof FarmBlock) {
                tier.setFarmland(() -> farmland);
            } else {
                ErrorManager.INSTANCE.addError(CATEGORY, "Modifying %s: %s".formatted(tier.getId(), "Invalid farmland block: %s".formatted(block)));
            }
        });

        ESSENCE_MAP.forEach((tier, item) -> {
            var essence = BuiltInRegistries.ITEM.get(item);
            if (essence != Items.AIR) {
                tier.setEssence(() -> essence);
            } else {
                ErrorManager.INSTANCE.addError(CATEGORY, "Modifying %s: %s".formatted(tier.getId(), "Invalid essence item: %s".formatted(item)));
            }
        });
    }
}
