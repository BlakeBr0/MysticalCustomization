package com.blakebr0.mysticalcustomization.create;

import com.blakebr0.mysticalagriculture.api.soul.IMobSoulType;
import com.blakebr0.mysticalagriculture.api.soul.MobSoulType;
import com.blakebr0.mysticalcustomization.util.ParsingUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;

import java.util.HashSet;
import java.util.Set;

public class MobSoulTypeCreator {
    public static IMobSoulType create(ResourceLocation id, JsonObject json) {
        float souls = JSONUtils.getFloat(json, "souls");

        MobSoulType type;
        if (json.has("entity")) {
            String entityId = JSONUtils.getString(json, "entity");
            ResourceLocation entity = new ResourceLocation(entityId);

            type = new MobSoulType(id, entity, null, souls, -1);
        } else if (json.has("entities")) {
            JsonArray entityIds = JSONUtils.getJsonArray(json, "entities");
            Set<ResourceLocation> entities = new HashSet<>();
            entityIds.forEach(entity -> entities.add(new ResourceLocation(entity.getAsString())));

            type = new MobSoulType(id, entities, null, souls, -1);
        } else {
            throw new JsonSyntaxException("Missing 'entity' or 'entities' property");
        }

        if (json.has("color")) {
            String color = JSONUtils.getString(json, "color");
            int i = ParsingUtils.parseHex(color, "color");
            type.setColor(i);
        }

        return type;
    }
}
