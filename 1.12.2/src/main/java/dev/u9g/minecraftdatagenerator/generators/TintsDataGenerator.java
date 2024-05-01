package dev.u9g.minecraftdatagenerator.generators;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dev.u9g.minecraftdatagenerator.mixin.BiomeAccessor;
import dev.u9g.minecraftdatagenerator.util.DGU;
import dev.u9g.minecraftdatagenerator.util.Registries;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.RedstoneWireBlock;
import net.minecraft.client.BlockColors;
import net.minecraft.client.color.world.FoliageColors;
import net.minecraft.client.color.world.GrassColors;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.biome.Biome;

import java.util.*;

public class TintsDataGenerator implements IDataGenerator {
    public static BiomeTintColors generateBiomeTintColors() {
        BiomeTintColors colors = new BiomeTintColors();

        for (Biome biome : Registries.BIOMES) {
            double d = MathHelper.clamp(biome.getTemperature(), 0.0f, 1.0f);
            double e = MathHelper.clamp(biome.getRainfall(), 0.0f, 1.0f);

            int biomeGrassColor = GrassColors.getColor(d, e);
            int biomeFoliageColor = FoliageColors.getColor(d, e);
            int biomeWaterColor = ((BiomeAccessor) biome).waterColor();

            colors.grassColoursMap.computeIfAbsent(biomeGrassColor, k -> new ArrayList<>()).add(biome);
            colors.foliageColoursMap.computeIfAbsent(biomeFoliageColor, k -> new ArrayList<>()).add(biome);
            colors.waterColourMap.computeIfAbsent(biomeWaterColor, k -> new ArrayList<>()).add(biome);
        }
        return colors;
    }

    public static Map<Integer, Integer> generateRedstoneTintColors() {
        Map<Integer, Integer> resultColors = new HashMap<>();

        for (int redstoneLevel : RedstoneWireBlock.POWER.getValues()) {
            int color = RedstoneWireBlock.getColorIntensity(redstoneLevel);
            resultColors.put(redstoneLevel, color);
        }
        return resultColors;
    }

    private static int getBlockColor(Block block) {
        return BlockColors.create().method_13410(block.getDefaultState(), DGU.getWorld(), BlockPos.ORIGIN);
    }

    public static Map<Block, Integer> generateConstantTintColors() {
        Map<Block, Integer> resultColors = new HashMap<>();
        // FIXME: ?
        // resultColors.put(Blocks.BIRCH_LEAVES, FoliageColors.getBirchColor());
        // resultColors.put(Blocks.SPRUCE_LEAVES, FoliageColors.getSpruceColor());

        resultColors.put(Blocks.LILY_PAD, getBlockColor(Blocks.LILY_PAD));
        // FIXME: ?
        // resultColors.put(Blocks.ATTACHED_MELON_STEM, getBlockColor(Blocks.ATTACHED_MELON_STEM, blockColors));
        // resultColors.put(Blocks.ATTACHED_PUMPKIN_STEM, getBlockColor(Blocks.ATTACHED_PUMPKIN_STEM, blockColors));

        //not really constant, depend on the block age, but kinda have to be handled since textures are literally white without them
        resultColors.put(Blocks.MELON_STEM, getBlockColor(Blocks.MELON_STEM));
        resultColors.put(Blocks.PUMPKIN_STEM, getBlockColor(Blocks.PUMPKIN_STEM));

        return resultColors;
    }

    private static JsonObject encodeBiomeColorMap(Map<Integer, List<Biome>> colorsMap) {
        JsonArray resultColorsArray = new JsonArray();
        for (Map.Entry<Integer, List<Biome>> entry : colorsMap.entrySet()) {
            JsonObject entryObject = new JsonObject();

            JsonArray keysArray = new JsonArray();
            for (Biome biome : entry.getValue()) {
                Identifier registryKey = Registries.BIOMES.getIdentifier(biome);
                keysArray.add(Objects.requireNonNull(registryKey).getPath());
            }

            entryObject.add("keys", keysArray);
            entryObject.addProperty("color", entry.getKey());
            resultColorsArray.add(entryObject);
        }

        JsonObject resultObject = new JsonObject();
        resultObject.add("data", resultColorsArray);
        return resultObject;
    }

    private static JsonObject encodeRedstoneColorMap(Map<Integer, Integer> colorsMap) {
        JsonArray resultColorsArray = new JsonArray();
        for (Map.Entry<Integer, Integer> entry : colorsMap.entrySet()) {
            JsonObject entryObject = new JsonObject();

            JsonArray keysArray = new JsonArray();
            keysArray.add(entry.getKey());

            entryObject.add("keys", keysArray);
            entryObject.addProperty("color", entry.getValue());
            resultColorsArray.add(entryObject);
        }

        JsonObject resultObject = new JsonObject();
        resultObject.add("data", resultColorsArray);
        return resultObject;
    }

    private static JsonObject encodeBlocksColorMap(Map<Block, Integer> colorsMap) {
        JsonArray resultColorsArray = new JsonArray();
        for (Map.Entry<Block, Integer> entry : colorsMap.entrySet()) {
            JsonObject entryObject = new JsonObject();

            JsonArray keysArray = new JsonArray();
            Identifier registryKey = Registries.BLOCKS.getIdentifier(entry.getKey());
            keysArray.add(Objects.requireNonNull(registryKey).getPath());

            entryObject.add("keys", keysArray);
            entryObject.addProperty("color", entry.getValue());
            resultColorsArray.add(entryObject);
        }

        JsonObject resultObject = new JsonObject();
        resultObject.add("data", resultColorsArray);
        return resultObject;
    }

    @Override
    public String getDataName() {
        return "tints";
    }

    @Override
    public JsonObject generateDataJson() {
        BiomeTintColors biomeTintColors = generateBiomeTintColors();
        Map<Integer, Integer> redstoneColors = generateRedstoneTintColors();
        Map<Block, Integer> constantTintColors = generateConstantTintColors();

        JsonObject resultObject = new JsonObject();

        resultObject.add("grass", encodeBiomeColorMap(biomeTintColors.grassColoursMap));
        resultObject.add("foliage", encodeBiomeColorMap(biomeTintColors.foliageColoursMap));
        resultObject.add("water", encodeBiomeColorMap(biomeTintColors.waterColourMap));

        resultObject.add("redstone", encodeRedstoneColorMap(redstoneColors));
        resultObject.add("constant", encodeBlocksColorMap(constantTintColors));

        return resultObject;
    }

    public static class BiomeTintColors {
        final Map<Integer, List<Biome>> grassColoursMap = new HashMap<>();
        final Map<Integer, List<Biome>> foliageColoursMap = new HashMap<>();
        final Map<Integer, List<Biome>> waterColourMap = new HashMap<>();
    }
}
