package com.blakebr0.mysticalcustomization.loader;

import com.blakebr0.mysticalagriculture.api.registry.IMobSoulTypeRegistry;
import com.blakebr0.mysticalagriculture.api.soul.MobSoulType;
import com.blakebr0.mysticalcustomization.MysticalCustomization;
import com.blakebr0.mysticalcustomization.create.MobSoulTypeCreator;
import com.blakebr0.mysticalcustomization.modify.MobSoulTypeModifier;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.loading.FMLPaths;
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
import java.util.List;
import java.util.Map;

public final class MobSoulTypeLoader {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    public static final Map<MobSoulType, List<ResourceLocation>> ENTITY_ADDITIONS_MAP = new HashMap<>();

    public static void onRegisterMobSoulTypes(IMobSoulTypeRegistry registry) {
        var dir = FMLPaths.CONFIGDIR.get().resolve("mysticalcustomization/mobsoultypes/").toFile();
        if (!dir.exists() && dir.mkdirs()) {
            MysticalCustomization.LOGGER.info("Created /config/mysticalcustomization/mobsoultypes/ directory");
        }

        var files = dir.listFiles((FileFilter) FileFilterUtils.suffixFileFilter(".json"));
        if (files == null)
            return;

        for (var file : files) {
            JsonObject json;
            InputStreamReader reader = null;
            ResourceLocation id = null;
            MobSoulType type = null;

            try {
                var parser = new JsonParser();
                reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8);
                json = parser.parse(reader).getAsJsonObject();
                var name = file.getName().replace(".json", "");
                id = new ResourceLocation(MysticalCustomization.MOD_ID, name);

                type = MobSoulTypeCreator.create(id, json);

                reader.close();
            } catch (Exception e) {
                MysticalCustomization.LOGGER.error("An error occurred while creating mob soul type with id {}", id, e);
            } finally {
                IOUtils.closeQuietly(reader);
            }

            if (type != null)
                registry.register(type);
        }
    }

    public static void onPostRegisterMobSoulTypes(IMobSoulTypeRegistry registry) {
        var dir = FMLPaths.CONFIGDIR.get().resolve("mysticalcustomization/").toFile();
        if (!dir.exists() && dir.mkdirs()) {
            MysticalCustomization.LOGGER.info("Created /config/mysticalcustomization/ directory");
        }

        var file = FMLPaths.CONFIGDIR.get().resolve("mysticalcustomization/configure-mobsoultypes.json").toFile();
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
                    var type = registry.getMobSoulTypeById(new ResourceLocation(id));

                    if (type == null) {
                        var error = String.format("Invalid mob soul type id provided: %s", id);
                        throw new JsonParseException(error);
                    }

                    MobSoulTypeModifier.modify(type, changes);
                });

                reader.close();
            } catch (Exception e) {
                MysticalCustomization.LOGGER.error("An error occurred while reading configure-mobsoultypes.json", e);
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
            entities.forEach(entity -> {
                var success = registry.addEntityTo(type, entity);

                if (!success) {
                    MysticalCustomization.LOGGER.error("Could not add entity {} to mob soul type {}, maybe it's already in use?", entity, type.getId());
                }
            });
        });
    }
}
