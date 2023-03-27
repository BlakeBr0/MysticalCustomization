package com.blakebr0.mysticalcustomization.create;

import com.blakebr0.cucumber.helper.ParsingHelper;
import com.blakebr0.mysticalagriculture.api.soul.MobSoulType;
import com.blakebr0.mysticalcustomization.loader.MobSoulTypeLoader;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

import java.util.ArrayList;

public final class MobSoulTypeCreator {
    public static MobSoulType create(ResourceLocation id, JsonObject json) throws JsonSyntaxException {
        var souls = GsonHelper.getAsFloat(json, "souls");

        var type = new MobSoulType(id, Sets.newHashSet(), null, souls, -1);

        if (json.has("entity")) {
            var entityId = GsonHelper.getAsString(json, "entity");
            var entity = new ResourceLocation(entityId);

            MobSoulTypeLoader.ENTITY_ADDITIONS_MAP.put(type, Lists.newArrayList(entity));
        } else if (json.has("entities")) {
            var entityIds = GsonHelper.getAsJsonArray(json, "entities");
            var entities = new ArrayList<ResourceLocation>();

            entityIds.forEach(entity -> {
                entities.add(new ResourceLocation(entity.getAsString()));
            });

            MobSoulTypeLoader.ENTITY_ADDITIONS_MAP.put(type, entities);
        } else {
            throw new JsonSyntaxException("Missing 'entity' or 'entities' property");
        }

        if (json.has("color")) {
            var color = GsonHelper.getAsString(json, "color");
            var i = ParsingHelper.parseHex(color, "color");

            type.setColor(i);
        }

        if (json.has("name")) {
            var name = GsonHelper.getAsString(json, "name");
            type.setEntityDisplayName(Component.literal(name));
        }

        if (json.has("enabled")) {
            var enabled = GsonHelper.getAsBoolean(json, "enabled");
            type.setEnabled(enabled);
        }

        return type;
    }
}
