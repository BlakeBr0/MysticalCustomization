package com.blakebr0.mysticalcustomization.modify;

import com.blakebr0.mysticalagriculture.api.MysticalAgricultureAPI;
import com.blakebr0.mysticalagriculture.api.soul.MobSoulType;
import com.blakebr0.mysticalcustomization.MysticalCustomization;
import com.blakebr0.mysticalcustomization.loader.MobSoulTypeLoader;
import com.blakebr0.mysticalcustomization.util.ParsingUtils;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

import java.util.ArrayList;

public final class MobSoulTypeModifier {
    public static void modify(MobSoulType type, JsonObject json) throws JsonSyntaxException {
        if (json.has("souls")) {
            var souls = GsonHelper.getAsFloat(json, "souls");
            type.setSoulRequirement(souls);
        }

        if (json.has("entities")) {
            var entities = GsonHelper.getAsJsonObject(json, "entities");

            if (entities.has("remove")) {
                var remove = GsonHelper.getAsJsonArray(entities, "remove");

                remove.forEach(entity -> {
                    boolean success = MysticalAgricultureAPI.getMobSoulTypeRegistry().removeEntityFrom(type, new ResourceLocation(entity.getAsString()));

                    if (!success) {
                        MysticalCustomization.LOGGER.error("Could not remove entity {} from mob soul type {}, maybe it wasn't added?", entity.getAsString(), type.getId());
                    }
                });
            }

            if (entities.has("add")) {
                var add = GsonHelper.getAsJsonArray(entities, "add");

                add.forEach(entity -> {
                    MobSoulTypeLoader.ENTITY_ADDITIONS_MAP.computeIfAbsent(type, t -> new ArrayList<>()).add(new ResourceLocation(entity.getAsString()));
                });
            }
        }

        if (json.has("color")) {
            var color = GsonHelper.getAsString(json, "color");
            var i = ParsingUtils.parseHex(color, "color");

            type.setColor(i);
        }

        if (json.has("name")) {
            var name = GsonHelper.getAsString(json, "name");
            type.setEntityDisplayName(new TextComponent(name));
        }

        if (json.has("enabled")) {
            var enabled = GsonHelper.getAsBoolean(json, "enabled");
            type.setEnabled(enabled);
        }
    }
}
