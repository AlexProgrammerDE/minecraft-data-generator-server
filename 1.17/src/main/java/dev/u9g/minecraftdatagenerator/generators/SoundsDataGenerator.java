package dev.u9g.minecraftdatagenerator.generators;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dev.u9g.minecraftdatagenerator.mixin.SoundAccessor;
import net.minecraft.client.sound.Sound;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.registry.Registry;

public class SoundsDataGenerator implements IDataGenerator {

    public static JsonObject generateSound(SoundEvent soundEvent) {
        JsonObject soundDesc = new JsonObject();

        soundDesc.addProperty("id", Registry.SOUND_EVENT.getRawId(soundEvent));
        soundDesc.addProperty("name", ((SoundAccessor) soundEvent).id().getPath());

        return soundDesc;
    }

    @Override
    public String getDataName() {
        return "sounds";
    }

    @Override
    public JsonArray generateDataJson() {
        JsonArray resultsArray = new JsonArray();
        Registry.SOUND_EVENT.forEach(sound -> resultsArray.add(generateSound(sound)));
        return resultsArray;
    }
}
