package com.blakebr0.mysticalcustomization.loader;

import com.blakebr0.mysticalagriculture.api.registry.IMobSoulTypeRegistry;
import com.blakebr0.mysticalagriculture.api.soul.MobSoulType;
import com.blakebr0.mysticalcustomization.MysticalCustomization;
import com.blakebr0.mysticalcustomization.create.MobSoulTypeCreator;
import com.blakebr0.mysticalcustomization.modify.MobSoulTypeModifier;
import com.blakebr0.mysticalcustomization.util.ErrorManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import net.minecraft.ResourceLocationException;
import net.minecraft.resources.ResourceLocation;
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
import java.util.List;
import java.util.Map;

public final class MobSoulTypeLoader {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final String CATEGORY = "Mob Soul Type";

    public static final Map<MobSoulType, List<ResourceLocation>> ENTITY_ADDITIONS_MAP = new HashMap<>();

    public static void onRegisterMobSoulTypes(IMobSoulTypeRegistry registry) {
        var dir = FMLPaths.CONFIGDIR.get().resolve("mysticalcustomization/mobsoultypes/").toFile();
        if (!dir.exists() && dir.mkdirs()) {
            MysticalCustomization.LOGGER.info("Created /config/mysticalcustomization/mobsoultypes/ directory");
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
                MobSoulType type = null;

                try {
                    reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8);
                    var json = JsonParser.parseReader(reader).getAsJsonObject();
                    var name = file.getName().replace(".json", "");
                    id = MysticalCustomization.resource(name);

                    try {
                        type = MobSoulTypeCreator.create(id, json);
                    } catch (JsonSyntaxException | ResourceLocationException e) {
                        ErrorManager.INSTANCE.addError(CATEGORY, "Creating %s: %s".formatted(id, e.getMessage()));
                    }

                    reader.close();
                } catch (Exception e) {
                    ErrorManager.INSTANCE.addFatalError(CATEGORY, "An error occurred creating mob soul type with id %s.".formatted(id), e);
                } finally {
                    IOUtils.closeQuietly(reader);
                }

                if (type != null)
                    registry.register(type);
            }
        } catch (Exception e) {
            ErrorManager.INSTANCE.addFatalError(CATEGORY, "An error occurred while processing mob soul type files.", e);
        }
    }

    public static void onPostRegisterMobSoulTypes(IMobSoulTypeRegistry registry) {
        var dir = FMLPaths.CONFIGDIR.get().resolve("mysticalcustomization/").toFile();
        if (!dir.exists() && dir.mkdirs()) {
            MysticalCustomization.LOGGER.info("Created /config/mysticalcustomization/ directory");
        }

        var file = FMLPaths.CONFIGDIR.get().resolve("mysticalcustomization/configure-mobsoultypes.json").toFile();
        if (file.exists() && file.isFile()) {
            FileReader reader = null;

            try {
                reader = new FileReader(file);
                var json = JsonParser.parseReader(reader).getAsJsonObject();

                for (var entry : json.entrySet()) {
                    var id = entry.getKey();
                    var changes = entry.getValue().getAsJsonObject();
                    var type = registry.getMobSoulTypeById(ResourceLocation.tryParse(id));

                    try {
                        if (type == null) {
                            throw new JsonSyntaxException("Unknown mob soul type id: %s".formatted(id));
                        }

                        MobSoulTypeModifier.modify(type, changes);
                    } catch (JsonSyntaxException | ResourceLocationException e) {
                        ErrorManager.INSTANCE.addError(CATEGORY, "Modifying %s: %s".formatted(id, e.getMessage()));
                    }
                }

                reader.close();
            } catch (Exception e) {
                ErrorManager.INSTANCE.addFatalError(CATEGORY, "An error occurred while reading configure-mobsoultypes.json.", e);
            } finally {
                IOUtils.closeQuietly(reader);
            }
        } else {
            try (var writer = new FileWriter(file)) {
                var object = new JsonObject();
                GSON.toJson(object, writer);
            } catch (IOException e) {
                MysticalCustomization.LOGGER.error("An error occurred while creating configure-mobsoultypes.json", e);
            }
        }

        ENTITY_ADDITIONS_MAP.forEach((type, entities) -> {
            for (var entity : entities) {
                var success = registry.addEntityTo(type, entity);

                if (!success) {
                    ErrorManager.INSTANCE.addError(CATEGORY, "Modifying %s: Could not add entity %s, maybe it's already in use?".formatted(type.getId(), entity));
                }
            }
        });
    }
}
