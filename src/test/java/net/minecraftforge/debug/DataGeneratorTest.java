/*
 * Minecraft Forge
 * Copyright (c) 2016-2019.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation version 2.1
 * of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package net.minecraftforge.debug;

import static net.minecraftforge.debug.DataGeneratorTest.MODID;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jline.utils.InputStreamReader;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.google.common.collect.ObjectArrays;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementFrame;
import net.minecraft.advancement.AdvancementRewards;
import net.minecraft.block.BarrelBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.FenceBlock;
import net.minecraft.block.FenceGateBlock;
import net.minecraft.block.FurnaceBlock;
import net.minecraft.block.LogBlock;
import net.minecraft.block.PaneBlock;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.block.TrapdoorBlock;
import net.minecraft.block.WallBlock;
import net.minecraft.client.render.model.json.JsonUnbakedModel.GuiLight;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.render.model.json.ModelVariant;
import net.minecraft.client.render.model.json.Transformation;
import net.minecraft.data.DataCache;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.server.BlockTagsProvider;
import net.minecraft.data.server.RecipesProvider;
import net.minecraft.data.server.recipe.RecipeJsonProvider;
import net.minecraft.data.server.recipe.ShapedRecipeJsonFactory;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Items;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceType;
import net.minecraft.tag.BlockTags;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.world.biome.Biomes;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ExistingFileHelper;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.client.model.generators.ModelBuilder;
import net.minecraftforge.client.model.generators.ModelBuilder.Perspective;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.client.model.generators.ModelFile.UncheckedModelFile;
import net.minecraftforge.client.model.generators.MultiPartBlockStateBuilder;
import net.minecraftforge.client.model.generators.VariantBlockStateBuilder;
import net.minecraftforge.common.crafting.ConditionalAdvancement;
import net.minecraftforge.common.crafting.ConditionalRecipe;
import net.minecraftforge.common.crafting.conditions.IConditionBuilder;
import net.minecraftforge.common.data.LanguageProvider;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;

@SuppressWarnings("deprecation")
@Mod(MODID)
@Mod.EventBusSubscriber(bus = Bus.MOD)
public class DataGeneratorTest
{
    static final String MODID = "data_gen_test";

    private static Gson GSON = null;

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event)
    {
        GSON = new GsonBuilder()
        .registerTypeAdapter(ModelVariant.class, new ModelVariant.Deserializer())
        .registerTypeAdapter(ModelTransformation.class, new ModelTransformation.Deserializer())
        .registerTypeAdapter(Transformation.class, new Transformation.Deserializer())
        .create();

        DataGenerator gen = event.getGenerator();

        if (event.includeClient())
        {
            gen.install(new Lang(gen));
            gen.install(new ItemModels(gen, event.getExistingFileHelper()));
            gen.install(new BlockStates(gen, event.getExistingFileHelper()));
        }
        if (event.includeServer())
        {
            gen.install(new Recipes(gen));
            gen.install(new Tags(gen));
        }
    }

    public static class Recipes extends RecipesProvider implements IConditionBuilder
    {
        public Recipes(DataGenerator gen)
        {
            super(gen);
        }

        protected void generate(Consumer<RecipeJsonProvider> consumer)
        {
            Identifier ID = new Identifier("data_gen_test", "conditional");

            ConditionalRecipe.builder()
            .addCondition(
                and(
                    not(modLoaded("minecraft")),
                    itemExists("minecraft", "dirt"),
                    FALSE()
                )
            )
            .addRecipe(
                ShapedRecipeJsonFactory.create(Blocks.field_10201, 64)
                .pattern("XXX")
                .pattern("XXX")
                .pattern("XXX")
                .input('X', Blocks.field_10566)
                .group("")
                .criterion("has_dirt", conditionsFromItem(Blocks.field_10566)) //Doesn't actually print... TODO: nested/conditional advancements?
                ::offerTo
            )
            .setAdvancement(ID,
                ConditionalAdvancement.builder()
                .addCondition(
                    and(
                        not(modLoaded("minecraft")),
                        itemExists("minecraft", "dirt"),
                        FALSE()
                    )
                )
                .addAdvancement(
                    Advancement.Task.create()
                    .parent(new Identifier("minecraft", "root"))
                    .display(Blocks.field_10201,
                        new LiteralText("Dirt2Diamonds"),
                        new LiteralText("The BEST crafting recipe in the game!"),
                        null, AdvancementFrame.field_1254, false, false, false
                    )
                    .rewards(AdvancementRewards.Builder.recipe(ID))
                    .criterion("has_dirt", conditionsFromItem(Blocks.field_10566))
                )
            )
            .build(consumer, ID);
        }
    }

    public static class Tags extends BlockTagsProvider
    {

        public Tags(DataGenerator gen)
        {
            super(gen);
        }

        @Override
        protected void configure()
        {
            getOrCreateTagBuilder(new BlockTags.CachingTag(new Identifier(MODID, "test")))
                .add(Blocks.field_10201)
                .add(BlockTags.field_15465)
                .addOptional(BlockTags.getContainer(), new Identifier("chisel", "marble/raw"))
                .addOptionalTag(new Identifier("forge", "storage_blocks/ruby"));
        }
    }

    public static class Lang extends LanguageProvider
    {
        public Lang(DataGenerator gen)
        {
            super(gen, MODID, "en_us");
        }

        @Override
        protected void addTranslations()
        {
            add(Blocks.field_10340, "Stone");
            add(Items.field_8477, "Diamond");
            add(Biomes.field_9434, "Beach");
            add(StatusEffects.field_5899, "Poison");
            add(Enchantments.field_9118, "Sharpness");
            add(EntityType.field_16281, "Cat");
            add(MODID + ".test.unicode", "\u0287s\u01DD\u2534 \u01DDpo\u0254\u1D09u\u2229");
        }
    }

    public static class ItemModels extends ItemModelProvider
    {
        private static final Logger LOGGER = LogManager.getLogger();

        public ItemModels(DataGenerator generator, ExistingFileHelper existingFileHelper)
        {
            super(generator, MODID, existingFileHelper);
        }

        @Override
        protected void registerModels()
        {
            getBuilder("test_generated_model")
                    .parent(new UncheckedModelFile("item/generated"))
                    .texture("layer0", mcLoc("block/stone"));

            getBuilder("test_block_model")
                    .parent(getExistingFile(mcLoc("block/block")))
                    .texture("all", mcLoc("block/dirt"))
                    .texture("top", mcLoc("block/stone"))
                    .element()
                        .cube("#all")
                        .face(Direction.field_11036)
                            .texture("#top")
                            .tintindex(0)
                            .end()
                        .end();

            // Testing consistency

            // Test overrides
            ModelFile fishingRod = withExistingParent("fishing_rod", "handheld_rod")
                .texture("layer0", mcLoc("item/fishing_rod"))
                .override()
                    .predicate(mcLoc("cast"), 1)
                    .model(getExistingFile(mcLoc("item/fishing_rod_cast"))) // Use the vanilla model for validation
                    .end();

            withExistingParent("fishing_rod_cast", modLoc("fishing_rod"))
                .parent(fishingRod)
                .texture("layer0", mcLoc("item/fishing_rod_cast"));
        }

        private static final Set<String> IGNORED_MODELS = ImmutableSet.of("test_generated_model", "test_block_model");

        @Override
        public void run(DataCache cache) throws IOException
        {
            super.run(cache);
            List<String> errors = testModelResults(this.generatedModels, existingFileHelper, IGNORED_MODELS.stream().map(s -> new Identifier(MODID, folder + "/" + s)).collect(Collectors.toSet()));
            if (!errors.isEmpty()) {
                LOGGER.error("Found {} discrepancies between generated and vanilla item models: ", errors.size());
                for (String s : errors) {
                    LOGGER.error("    {}", s);
                }
                throw new AssertionError("Generated item models differed from vanilla equivalents, check above errors.");
            }
        }

        @Override
        public String getName()
        {
            return "Forge Test Item Models";
        }
    }

    public static class BlockStates extends BlockStateProvider
    {
        private static final Logger LOGGER = LogManager.getLogger();

        public BlockStates(DataGenerator gen, ExistingFileHelper exFileHelper)
        {
            super(gen, MODID, exFileHelper);
        }

        @Override
        protected void registerStatesAndModels()
        {
            // Unnecessarily complicated example to showcase how manual building works
            ModelFile birchFenceGate = models().fenceGate("birch_fence_gate", mcLoc("block/birch_planks"));
            ModelFile birchFenceGateOpen = models().fenceGateOpen("birch_fence_gate_open", mcLoc("block/birch_planks"));
            ModelFile birchFenceGateWall = models().fenceGateWall("birch_fence_gate_wall", mcLoc("block/birch_planks"));
            ModelFile birchFenceGateWallOpen = models().fenceGateWallOpen("birch_fence_gate_wall_open", mcLoc("block/birch_planks"));
            ModelFile invisbleModel = new UncheckedModelFile(new Identifier("builtin/generated"));
            VariantBlockStateBuilder builder = getVariantBuilder(Blocks.field_10513);
            for (Direction dir : FenceGateBlock.FACING.getValues()) {
                int angle = (int) dir.asRotation();
                builder
                    .partialState()
                        .with(FenceGateBlock.FACING, dir)
                        .with(FenceGateBlock.IN_WALL, false)
                        .with(FenceGateBlock.OPEN, false)
                        .modelForState()
                            .modelFile(invisbleModel)
                        .nextModel()
                            .modelFile(birchFenceGate)
                            .rotationY(angle)
                            .uvLock(true)
                            .weight(100)
                        .addModel()
                    .partialState()
                        .with(FenceGateBlock.FACING, dir)
                        .with(FenceGateBlock.IN_WALL, false)
                        .with(FenceGateBlock.OPEN, true)
                        .modelForState()
                            .modelFile(birchFenceGateOpen)
                            .rotationY(angle)
                            .uvLock(true)
                        .addModel()
                    .partialState()
                        .with(FenceGateBlock.FACING, dir)
                        .with(FenceGateBlock.IN_WALL, true)
                        .with(FenceGateBlock.OPEN, false)
                        .modelForState()
                            .modelFile(birchFenceGateWall)
                            .rotationY(angle)
                            .uvLock(true)
                        .addModel()
                    .partialState()
                        .with(FenceGateBlock.FACING, dir)
                        .with(FenceGateBlock.IN_WALL, true)
                        .with(FenceGateBlock.OPEN, true)
                        .modelForState()
                            .modelFile(birchFenceGateWallOpen)
                            .rotationY(angle)
                            .uvLock(true)
                        .addModel();
            }

            // Realistic examples using helpers
            simpleBlock(Blocks.field_10340, model -> ObjectArrays.concat(
                    ConfiguredModel.allYRotations(model, 0, false),
                    ConfiguredModel.allYRotations(model, 180, false),
                    ConfiguredModel.class));

            // From here on, models are 1-to-1 copies of vanilla (except for model locations) and will be tested as such below
            ModelFile block = models().getBuilder("block")
                .guiLight(GuiLight.field_21859)
                .transforms()
                    .transform(Perspective.GUI)
                        .rotation(30, 225, 0)
                        .scale(0.625f)
                        .end()
                    .transform(Perspective.GROUND)
                        .translation(0, 3, 0)
                        .scale(0.25f)
                        .end()
                    .transform(Perspective.FIXED)
                        .scale(0.5f)
                        .end()
                    .transform(Perspective.THIRDPERSON_RIGHT)
                        .rotation(75, 45, 0)
                        .translation(0, 2.5f, 0)
                        .scale(0.375f)
                        .end()
                    .transform(Perspective.FIRSTPERSON_RIGHT)
                        .rotation(0, 45, 0)
                        .scale(0.4f)
                        .end()
                    .transform(Perspective.FIRSTPERSON_LEFT)
                        .rotation(0, 225, 0)
                        .scale(0.4f)
                        .end()
                    .end();

            models().getBuilder("cube")
                .parent(block)
                .element()
                    .allFaces((dir, face) -> face.texture("#" + dir.asString()).cullface(dir));

            ModelFile furnace = models().orientable("furnace", mcLoc("block/furnace_side"), mcLoc("block/furnace_front"), mcLoc("block/furnace_top"));
            ModelFile furnaceLit = models().orientable("furnace_on", mcLoc("block/furnace_side"), mcLoc("block/furnace_front_on"), mcLoc("block/furnace_top"));

            getVariantBuilder(Blocks.field_10181)
                .forAllStates(state -> ConfiguredModel.builder()
                        .modelFile(state.get(FurnaceBlock.LIT) ? furnaceLit : furnace)
                        .rotationY((int) state.get(FurnaceBlock.FACING).getOpposite().asRotation())
                        .build()
                );

            ModelFile barrel = models().cubeBottomTop("barrel", mcLoc("block/barrel_side"), mcLoc("block/barrel_bottom"), mcLoc("block/barrel_top"));
            ModelFile barrelOpen = models().cubeBottomTop("barrel_open", mcLoc("block/barrel_side"), mcLoc("block/barrel_bottom"), mcLoc("block/barrel_top_open"));
            directionalBlock(Blocks.field_16328, state -> state.get(BarrelBlock.OPEN) ? barrelOpen : barrel); // Testing custom state interpreter

            logBlock((LogBlock) Blocks.field_10533);

            stairsBlock((StairsBlock) Blocks.field_10256, "acacia", mcLoc("block/acacia_planks"));
            slabBlock((SlabBlock) Blocks.field_10031, Blocks.field_10218.getRegistryName(), mcLoc("block/acacia_planks"));

            fenceBlock((FenceBlock) Blocks.field_10144, "acacia", mcLoc("block/acacia_planks"));
            fenceGateBlock((FenceGateBlock) Blocks.field_10457, "acacia", mcLoc("block/acacia_planks"));

            wallBlock((WallBlock) Blocks.field_10625, "cobblestone", mcLoc("block/cobblestone"));

            paneBlock((PaneBlock) Blocks.field_10285, "glass", mcLoc("block/glass"), mcLoc("block/glass_pane_top"));

            doorBlock((DoorBlock) Blocks.field_10232, "acacia", mcLoc("block/acacia_door_bottom"), mcLoc("block/acacia_door_top"));
            trapdoorBlock((TrapdoorBlock) Blocks.field_10608, "acacia", mcLoc("block/acacia_trapdoor"), true);
            trapdoorBlock((TrapdoorBlock) Blocks.field_10137, "oak", mcLoc("block/oak_trapdoor"), false); // Test a non-orientable trapdoor

            simpleBlock(Blocks.field_10336, models().torch("torch", mcLoc("block/torch")));
            horizontalBlock(Blocks.field_10099, models().torchWall("wall_torch", mcLoc("block/torch")), 90);
        }

        // Testing the outputs

        private static final Set<Block> IGNORED_BLOCKS = ImmutableSet.of(Blocks.field_10513, Blocks.field_10340);
        private static final Set<Identifier> IGNORED_MODELS = ImmutableSet.of();

        private List<String> errors = new ArrayList<>();

        @Override
        public void run(DataCache cache) throws IOException
        {
            super.run(cache);
            this.errors.addAll(testModelResults(models().generatedModels, models().existingFileHelper, IGNORED_MODELS));
            this.registeredBlocks.forEach((block, state) -> {
                if (IGNORED_BLOCKS.contains(block)) return;
                JsonObject generated = state.toJson();
                try {
                    Resource vanillaResource = models().existingFileHelper.getResource(block.getRegistryName(), ResourceType.field_14188, ".json", "blockstates");
                    JsonObject existing = GSON.fromJson(new InputStreamReader(vanillaResource.getInputStream()), JsonObject.class);
                    if (state instanceof VariantBlockStateBuilder) {
                        compareVariantBlockstates(block, generated, existing);
                    } else if (state instanceof MultiPartBlockStateBuilder) {
                        compareMultipartBlockstates(block, generated, existing);
                    } else {
                        throw new IllegalStateException("Unknown blockstate type: " + state.getClass());
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });

            if (!errors.isEmpty()) {
                LOGGER.error("Found {} discrepancies between generated and vanilla models/blockstates: ", errors.size());
                for (String s : errors) {
                    LOGGER.error("    {}", s);
                }
                throw new AssertionError("Generated blockstates/models differed from vanilla equivalents, check above errors.");
            }
        }

        private void compareVariantBlockstates(Block block, JsonObject generated, JsonObject vanilla) {
            JsonObject generatedVariants = generated.getAsJsonObject("variants");
            JsonObject vanillaVariants = vanilla.getAsJsonObject("variants");
            Stream.concat(generatedVariants.entrySet().stream(), vanillaVariants.entrySet().stream())
                .map(e -> e.getKey())
                .distinct()
                .forEach(key -> {
                    JsonElement generatedVariant = generatedVariants.get(key);
                    JsonElement vanillaVariant = vanillaVariants.get(key);
                    if (generatedVariant.isJsonArray()) {
                        compareArrays(block, "key " + key, "random variants", generatedVariant, vanillaVariant);
                        for (int i = 0; i < generatedVariant.getAsJsonArray().size(); i++) {
                            compareVariant(block, key + "[" + i + "]", generatedVariant.getAsJsonArray().get(i).getAsJsonObject(), vanillaVariant.getAsJsonArray().get(i).getAsJsonObject());
                        }
                    }
                    if (generatedVariant.isJsonObject()) {
                        if (!vanillaVariant.isJsonObject()) {
                            blockstateError(block, "incorrectly does not have an array of variants for key %s", key);
                            return;
                        }
                        compareVariant(block, key, generatedVariant.getAsJsonObject(), vanillaVariant.getAsJsonObject());
                    }
                });
        }

        private void compareVariant(Block block, String key, JsonObject generatedVariant, JsonObject vanillaVariant) {
            if (generatedVariant == null) {
                blockstateError(block, "missing variant for %s", key);
                return;
            }
            if (vanillaVariant == null) {
                blockstateError(block, "has extra variant %s", key);
                return;
            }
            String generatedModel = toVanillaModel(generatedVariant.get("model").getAsString());
            String vanillaModel = vanillaVariant.get("model").getAsString();
            if (!generatedModel.equals(vanillaModel)) {
                blockstateError(block, "has incorrect model \"%s\" for variant %s. Expecting: %s", generatedModel, key, vanillaModel);
                return;
            }
            generatedVariant.addProperty("model", generatedModel);
            // Parse variants to objects to handle default values in vanilla jsons
            ModelVariant parsedGeneratedVariant = GSON.fromJson(generatedVariant, ModelVariant.class);
            ModelVariant parsedVanillaVariant = GSON.fromJson(vanillaVariant, ModelVariant.class);
            if (!parsedGeneratedVariant.equals(parsedVanillaVariant)) {
                blockstateError(block, "has incorrect variant %s. Expecting: %s, Found: %s", key, vanillaVariant, generatedVariant);
                return;
            }
        }

        private void compareMultipartBlockstates(Block block, JsonObject generated, JsonObject vanilla) {
            JsonElement generatedPartsElement = generated.get("multipart");
            JsonElement vanillaPartsElement = vanilla.getAsJsonArray("multipart");
            compareArrays(block, "parts", "multipart", generatedPartsElement, vanillaPartsElement);
            // String instead of JSON types due to inconsistent hashing
            Multimap<String, String> generatedPartsByCondition = HashMultimap.create();
            Multimap<String, String> vanillaPartsByCondition = HashMultimap.create();

            JsonArray generatedParts = generatedPartsElement.getAsJsonArray();
            JsonArray vanillaParts = vanillaPartsElement.getAsJsonArray();
            for (int i = 0; i < generatedParts.size(); i++) {
                JsonObject generatedPart = generatedParts.get(i).getAsJsonObject();
                String generatedCondition = toEquivalentString(generatedPart.get("when"));
                JsonElement generatedVariants = generatedPart.get("apply");
                if (generatedVariants.isJsonObject()) {
                    correctVariant(generatedVariants.getAsJsonObject());
                } else if (generatedVariants.isJsonArray()) {
                    for (int j = 0; j < generatedVariants.getAsJsonArray().size(); j++) {
                        correctVariant(generatedVariants.getAsJsonArray().get(i).getAsJsonObject());
                    }
                }
                generatedPartsByCondition.put(generatedCondition, toEquivalentString(generatedVariants));

                JsonObject vanillaPart = vanillaParts.get(i).getAsJsonObject();
                String vanillaCondition = toEquivalentString(vanillaPart.get("when"));
                String vanillaVariants = toEquivalentString(vanillaPart.get("apply"));

                vanillaPartsByCondition.put(vanillaCondition, vanillaVariants);
            }

            Stream.concat(generatedPartsByCondition.keySet().stream(), vanillaPartsByCondition.keySet().stream())
                .distinct()
                .forEach(cond -> {
                    Collection<String> generatedVariants = generatedPartsByCondition.get(cond);
                    Collection<String> vanillaVariants = vanillaPartsByCondition.get(cond);
                    if (generatedVariants.size() != vanillaVariants.size()) {
                        if (vanillaVariants.isEmpty()) {
                            blockstateError(block, " has extra condition %s", cond);
                        } else if (generatedVariants.isEmpty()) {
                            blockstateError(block, " is missing condition %s", cond);
                        } else {
                            blockstateError(block, " has differing amounts of variant lists matching condition %s. Expected: %d, Found: %d", cond, vanillaVariants.size(), generatedVariants.size());
                        }
                        return;
                    }

                    if (!vanillaVariants.containsAll(generatedVariants) || !generatedVariants.containsAll(vanillaVariants)) {
                        List<String> extra = new ArrayList<>(generatedVariants);
                        extra.removeAll(vanillaVariants);
                        List<String> missing = new ArrayList<>(vanillaVariants);
                        missing.removeAll(generatedVariants);
                        if (!extra.isEmpty()) {
                            blockstateError(block, " has extra variants for condition %s: %s", cond, extra);
                        }
                        if (!missing.isEmpty()) {
                            blockstateError(block, " has missing variants for condition %s: %s", cond, missing);
                        }
                    }
                });
        }

        // Eliminate some formatting differences that are not meaningful
        private String toEquivalentString(JsonElement element) {
            return Objects.toString(element)
                    .replaceAll("\"(true|false)\"", "$1") // Unwrap booleans in strings
                    .replaceAll("\"(-?(?:0|[1-9]\\d*)(?:\\.\\d+)?(?:[eE][+-]?\\d+)?)\"", "$1"); // Unwrap numbers in strings, regex from https://stackoverflow.com/questions/13340717/json-numbers-regular-expression
        }

        private void correctVariant(JsonObject variant) {
            variant.addProperty("model", toVanillaModel(variant.get("model").getAsString()));
        }

        private boolean compareArrays(Block block, String key, String name, JsonElement generated, JsonElement vanilla) {
            if (!vanilla.isJsonArray()) {
                blockstateError(block, "incorrectly has an array of %s for %s", name, key);
                return false;
            }
            JsonArray generatedArray = generated.getAsJsonArray();
            JsonArray vanillaArray = vanilla.getAsJsonArray();
            if (generatedArray.size() != vanillaArray.size()) {
                blockstateError(block, "has incorrect number of %s for %s. Expecting: %s, Found: %s", name, key, vanillaArray.size(), generatedArray.size());
                return false;
            }
            return true;
        }

        private void blockstateError(Block block, String fmt, Object... args) {
            errors.add("Generated blockstate for block " + block + " " + String.format(fmt, args));
        }

        @Override
        public String getName() {
            return "Forge Test Blockstates";
        }
    }

    private static <T extends ModelBuilder<T>> List<String> testModelResults(Map<Identifier, T> models, ExistingFileHelper existingFileHelper, Set<Identifier> toIgnore) {
        List<String> ret = new ArrayList<>();
        models.forEach((loc, model) -> {
            if (toIgnore.contains(loc)) return;
            JsonObject generated = model.toJson();
            if (generated.has("parent")) {
                generated.addProperty("parent", toVanillaModel(generated.get("parent").getAsString()));
            }
            try {
                Resource vanillaResource = existingFileHelper.getResource(new Identifier(loc.getPath()), ResourceType.field_14188, ".json", "models");
                JsonObject existing = GSON.fromJson(new InputStreamReader(vanillaResource.getInputStream()), JsonObject.class);

                JsonElement generatedDisplay = generated.remove("display");
                JsonElement vanillaDisplay = existing.remove("display");
                if (generatedDisplay == null && vanillaDisplay != null) {
                    ret.add("Model " + loc + " is missing transforms");
                    return;
                } else if (generatedDisplay != null && vanillaDisplay == null) {
                    ret.add("Model " + loc + " has transforms when vanilla equivalent does not");
                    return;
                } else if (generatedDisplay != null) { // Both must be non-null
                    ModelTransformation generatedTransforms = GSON.fromJson(generatedDisplay, ModelTransformation.class);
                    ModelTransformation vanillaTransforms = GSON.fromJson(vanillaDisplay, ModelTransformation.class);
                    for (Perspective type : Perspective.values()) {
                        if (!generatedTransforms.getTransformation(type.vanillaType).equals(vanillaTransforms.getTransformation(type.vanillaType))) {
                            ret.add("Model " + loc  + " has transforms that differ from vanilla equivalent for perspective " + type.name());
                            return;
                        }
                    }
                }

                if (!existing.equals(generated)) {
                    ret.add("Model " + loc + " does not match vanilla equivalent");
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        return ret;
    }

    private static String toVanillaModel(String model) {
        // We generate our own model jsons to test model building, but otherwise our blockstates should be identical
        // So remove modid to match
        return model.replaceAll("^\\w+:", "");
    }
}
