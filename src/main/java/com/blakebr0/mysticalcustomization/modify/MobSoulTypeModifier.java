package com.blakebr0.mysticalcustomization.modify;

import com.blakebr0.mysticalagriculture.api.MysticalAgricultureAPI;
import com.blakebr0.mysticalagriculture.api.soul.IMobSoulType;
import com.blakebr0.mysticalcustomization.MysticalCustomization;
import com.blakebr0.mysticalcustomization.loader.MobSoulTypeLoader;
import com.blakebr0.mysticalcustomization.util.ParsingUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;

public final class MobSoulTypeModifier {
    private static final Logger LOGGER = LogManager.getLogger(MysticalCustomization.NAME);

    public static void modify(IMobSoulType type, JsonObject json) throws JsonSyntaxException {
        if (json.has("souls")) {
            float souls = JSONUtils.getFloat(json, "souls");
            type.setSoulRequirement(souls);
        }

        if (json.has("entities")) {
            JsonObject entities = JSONUtils.getJsonObject(json, "entities");

            if (entities.has("remove")) {
                JsonArray remove = JSONUtils.getJsonArray(entities, "remove");

                remove.forEach(entity -> {
                    boolean success = MysticalAgricultureAPI.getMobSoulTypeRegistry().removeEntityFrom(type, new ResourceLocation(entity.getAsString()));

                    if (!success) {
                        LOGGER.error("Could not remove entity {} from mob soul type {}, maybe it wasn't added?", entity.getAsString(), type.getId());
                    }
                });
            }

            if (entities.has("add")) {
                JsonArray add = JSONUtils.getJsonArray(entities, "add");

                add.forEach(entity -> {
                    MobSoulTypeLoader.ENTITY_ADDITIONS_MAP.computeIfAbsent(type, t -> new ArrayList<>()).add(new ResourceLocation(entity.getAsString()));
                });
            }
        }

        if (json.has("color")) {
            String color = JSONUtils.getString(json, "color");
            int i = ParsingUtils.parseHex(color, "color");
            type.setColor(i);
        }

        if (json.has("name")) {
            String name = JSONUtils.getString(json, "name");
            type.setEntityDisplayName(new StringTextComponent(name));
        }

        if (json.has("enabled")) {
            boolean enabled = JSONUtils.getBoolean(json, "enabled");
            type.setEnabled(enabled);
        }
    }
}
