package com.blakebr0.mysticalcustomization.loader;

import com.blakebr0.mysticalagriculture.api.registry.IAugmentRegistry;
import com.blakebr0.mysticalcustomization.MysticalCustomization;
import com.blakebr0.mysticalcustomization.modify.AugmentModifier;
import com.blakebr0.mysticalcustomization.util.ErrorManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import net.minecraft.IdentifierException;
import net.minecraft.resources.Identifier;
import net.neoforged.fml.loading.FMLPaths;
import org.apache.commons.io.IOUtils;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public final class AugmentLoader {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final String CATEGORY = "Augment";

    public static void onPostRegisterAugments(IAugmentRegistry registry) {
        var dir = FMLPaths.CONFIGDIR.get().resolve("mysticalcustomization/").toFile();
        if (!dir.exists() && dir.mkdirs()) {
            MysticalCustomization.LOGGER.info("Created /config/mysticalcustomization/ directory");
        }

        var file = FMLPaths.CONFIGDIR.get().resolve("mysticalcustomization/configure-augments.json").toFile();
        if (file.exists() && file.isFile()) {
            InputStreamReader reader = null;

            try {
                reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8);
                var json = JsonParser.parseReader(reader).getAsJsonObject();

                for (var entry : json.entrySet()) {
                    var id = entry.getKey();
                    var changes = entry.getValue().getAsJsonObject();
                    var augment = registry.getAugmentById(Identifier.tryParse(id));

                    try {
                        if (augment == null) {
                            throw new JsonSyntaxException("Unknown augment id: %s".formatted(id));
                        }

                        AugmentModifier.modify(augment, changes);
                    } catch (JsonSyntaxException | IdentifierException e) {
                        ErrorManager.INSTANCE.addError(CATEGORY, "Modifying %s: %s".formatted(id, e.getMessage()));
                    }
                }

                reader.close();
            } catch (Exception e) {
                ErrorManager.INSTANCE.addFatalError(CATEGORY, "An error occurred while reading configure-augments.json.", e);
            } finally {
                IOUtils.closeQuietly(reader);
            }
        } else {
            try (var writer = new FileWriter(file)) {
                var object = new JsonObject();
                GSON.toJson(object, writer);
            } catch (IOException e) {
                MysticalCustomization.LOGGER.error("An error occurred while creating configure-augment.json", e);
            }
        }
    }
}
