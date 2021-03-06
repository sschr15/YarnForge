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

package net.minecraftforge.common.data;

import java.nio.file.Path;
import java.util.Locale;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import net.minecraft.block.Block;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.server.ItemTagsProvider;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.tag.Tag;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraftforge.common.Tags;
import net.minecraftforge.registries.ForgeRegistries;

public class ForgeItemTagsProvider extends ItemTagsProvider
{
    private Set<Identifier> filter = null;

    public ForgeItemTagsProvider(DataGenerator gen)
    {
        super(gen);
    }

    @Override
    public void configure()
    {
        super.configure();
        filter = this.tagBuilders.entrySet().stream().map(e -> e.getKey().getId()).collect(Collectors.toSet());

        getOrCreateTagBuilder(Tags.Items.ARROWS).add(Items.field_8107, Items.field_8087, Items.field_8236);
        getOrCreateTagBuilder(Tags.Items.BEACON_PAYMENT).add(Items.field_8687, Items.field_8477, Items.field_8695, Items.field_8620);
        getOrCreateTagBuilder(Tags.Items.BONES).add(Items.field_8606);
        getOrCreateTagBuilder(Tags.Items.BOOKSHELVES).add(Items.BOOKSHELF);
        copy(Tags.Blocks.CHESTS, Tags.Items.CHESTS);
        copy(Tags.Blocks.CHESTS_ENDER, Tags.Items.CHESTS_ENDER);
        copy(Tags.Blocks.CHESTS_TRAPPED, Tags.Items.CHESTS_TRAPPED);
        copy(Tags.Blocks.CHESTS_WOODEN, Tags.Items.CHESTS_WOODEN);
        copy(Tags.Blocks.COBBLESTONE, Tags.Items.COBBLESTONE);
        getOrCreateTagBuilder(Tags.Items.CROPS).add(Tags.Items.CROPS_BEETROOT, Tags.Items.CROPS_CARROT, Tags.Items.CROPS_NETHER_WART, Tags.Items.CROPS_POTATO, Tags.Items.CROPS_WHEAT);
        getOrCreateTagBuilder(Tags.Items.CROPS_BEETROOT).add(Items.field_8186);
        getOrCreateTagBuilder(Tags.Items.CROPS_CARROT).add(Items.field_8179);
        getOrCreateTagBuilder(Tags.Items.CROPS_NETHER_WART).add(Items.field_8790);
        getOrCreateTagBuilder(Tags.Items.CROPS_POTATO).add(Items.field_8567);
        getOrCreateTagBuilder(Tags.Items.CROPS_WHEAT).add(Items.field_8861);
        getOrCreateTagBuilder(Tags.Items.DUSTS).add(Tags.Items.DUSTS_GLOWSTONE, Tags.Items.DUSTS_PRISMARINE, Tags.Items.DUSTS_REDSTONE);
        getOrCreateTagBuilder(Tags.Items.DUSTS_GLOWSTONE).add(Items.field_8601);
        getOrCreateTagBuilder(Tags.Items.DUSTS_PRISMARINE).add(Items.field_8662);
        getOrCreateTagBuilder(Tags.Items.DUSTS_REDSTONE).add(Items.field_8725);
        addColored(getOrCreateTagBuilder(Tags.Items.DYES)::add, Tags.Items.DYES, "{color}_dye");
        getOrCreateTagBuilder(Tags.Items.EGGS).add(Items.field_8803);
        copy(Tags.Blocks.END_STONES, Tags.Items.END_STONES);
        getOrCreateTagBuilder(Tags.Items.ENDER_PEARLS).add(Items.field_8634);
        getOrCreateTagBuilder(Tags.Items.FEATHERS).add(Items.field_8153);
        copy(Tags.Blocks.FENCE_GATES, Tags.Items.FENCE_GATES);
        copy(Tags.Blocks.FENCE_GATES_WOODEN, Tags.Items.FENCE_GATES_WOODEN);
        copy(Tags.Blocks.FENCES, Tags.Items.FENCES);
        copy(Tags.Blocks.FENCES_NETHER_BRICK, Tags.Items.FENCES_NETHER_BRICK);
        copy(Tags.Blocks.FENCES_WOODEN, Tags.Items.FENCES_WOODEN);
        getOrCreateTagBuilder(Tags.Items.GEMS).add(Tags.Items.GEMS_DIAMOND, Tags.Items.GEMS_EMERALD, Tags.Items.GEMS_LAPIS, Tags.Items.GEMS_PRISMARINE, Tags.Items.GEMS_QUARTZ);
        getOrCreateTagBuilder(Tags.Items.GEMS_DIAMOND).add(Items.field_8477);
        getOrCreateTagBuilder(Tags.Items.GEMS_EMERALD).add(Items.field_8687);
        getOrCreateTagBuilder(Tags.Items.GEMS_LAPIS).add(Items.field_8759);
        getOrCreateTagBuilder(Tags.Items.GEMS_PRISMARINE).add(Items.field_8434);
        getOrCreateTagBuilder(Tags.Items.GEMS_QUARTZ).add(Items.field_8155);
        copy(Tags.Blocks.GLASS, Tags.Items.GLASS);
        copyColored(Tags.Blocks.GLASS, Tags.Items.GLASS);
        copy(Tags.Blocks.GLASS_PANES, Tags.Items.GLASS_PANES);
        copyColored(Tags.Blocks.GLASS_PANES, Tags.Items.GLASS_PANES);
        copy(Tags.Blocks.GRAVEL, Tags.Items.GRAVEL);
        getOrCreateTagBuilder(Tags.Items.GUNPOWDER).add(Items.field_8054);
        getOrCreateTagBuilder(Tags.Items.HEADS).add(Items.CREEPER_HEAD, Items.DRAGON_HEAD, Items.PLAYER_HEAD, Items.SKELETON_SKULL, Items.WITHER_SKELETON_SKULL, Items.ZOMBIE_HEAD);
        getOrCreateTagBuilder(Tags.Items.INGOTS).add(Tags.Items.INGOTS_IRON, Tags.Items.INGOTS_GOLD, Tags.Items.INGOTS_BRICK, Tags.Items.INGOTS_NETHER_BRICK);
        getOrCreateTagBuilder(Tags.Items.INGOTS_BRICK).add(Items.field_8621);
        getOrCreateTagBuilder(Tags.Items.INGOTS_GOLD).add(Items.field_8695);
        getOrCreateTagBuilder(Tags.Items.INGOTS_IRON).add(Items.field_8620);
        getOrCreateTagBuilder(Tags.Items.INGOTS_NETHER_BRICK).add(Items.field_8729);
        getOrCreateTagBuilder(Tags.Items.LEATHER).add(Items.field_8745);
        getOrCreateTagBuilder(Tags.Items.MUSHROOMS).add(Items.BROWN_MUSHROOM, Items.RED_MUSHROOM);
        getOrCreateTagBuilder(Tags.Items.MUSIC_DISCS).add(Items.field_8144, Items.field_8075, Items.field_8425, Items.field_8623, Items.field_8502, Items.field_8534, Items.field_8344, Items.field_8834, Items.field_8065, Items.field_8355, Items.field_8731, Items.field_8806);
        getOrCreateTagBuilder(Tags.Items.NETHER_STARS).add(Items.field_8137);
        copy(Tags.Blocks.NETHERRACK, Tags.Items.NETHERRACK);
        getOrCreateTagBuilder(Tags.Items.NUGGETS).add(Tags.Items.NUGGETS_IRON, Tags.Items.NUGGETS_GOLD);
        getOrCreateTagBuilder(Tags.Items.NUGGETS_IRON).add(Items.field_8675);
        getOrCreateTagBuilder(Tags.Items.NUGGETS_GOLD).add(Items.field_8397);
        copy(Tags.Blocks.OBSIDIAN, Tags.Items.OBSIDIAN);
        copy(Tags.Blocks.ORES, Tags.Items.ORES);
        copy(Tags.Blocks.ORES_COAL, Tags.Items.ORES_COAL);
        copy(Tags.Blocks.ORES_DIAMOND, Tags.Items.ORES_DIAMOND);
        copy(Tags.Blocks.ORES_EMERALD, Tags.Items.ORES_EMERALD);
        copy(Tags.Blocks.ORES_GOLD, Tags.Items.ORES_GOLD);
        copy(Tags.Blocks.ORES_IRON, Tags.Items.ORES_IRON);
        copy(Tags.Blocks.ORES_LAPIS, Tags.Items.ORES_LAPIS);
        copy(Tags.Blocks.ORES_QUARTZ, Tags.Items.ORES_QUARTZ);
        copy(Tags.Blocks.ORES_REDSTONE, Tags.Items.ORES_REDSTONE);
        getOrCreateTagBuilder(Tags.Items.RODS).add(Tags.Items.RODS_BLAZE, Tags.Items.RODS_WOODEN);
        getOrCreateTagBuilder(Tags.Items.RODS_BLAZE).add(Items.field_8894);
        getOrCreateTagBuilder(Tags.Items.RODS_WOODEN).add(Items.field_8600);
        copy(Tags.Blocks.SAND, Tags.Items.SAND);
        copy(Tags.Blocks.SAND_COLORLESS, Tags.Items.SAND_COLORLESS);
        copy(Tags.Blocks.SAND_RED, Tags.Items.SAND_RED);
        copy(Tags.Blocks.SANDSTONE, Tags.Items.SANDSTONE);
        getOrCreateTagBuilder(Tags.Items.SEEDS).add(Tags.Items.SEEDS_BEETROOT, Tags.Items.SEEDS_MELON, Tags.Items.SEEDS_PUMPKIN, Tags.Items.SEEDS_WHEAT);
        getOrCreateTagBuilder(Tags.Items.SEEDS_BEETROOT).add(Items.field_8309);
        getOrCreateTagBuilder(Tags.Items.SEEDS_MELON).add(Items.field_8188);
        getOrCreateTagBuilder(Tags.Items.SEEDS_PUMPKIN).add(Items.field_8706);
        getOrCreateTagBuilder(Tags.Items.SEEDS_WHEAT).add(Items.field_8317);
        getOrCreateTagBuilder(Tags.Items.SLIMEBALLS).add(Items.field_8777);
        copy(Tags.Blocks.STAINED_GLASS, Tags.Items.STAINED_GLASS);
        copy(Tags.Blocks.STAINED_GLASS_PANES, Tags.Items.STAINED_GLASS_PANES);
        copy(Tags.Blocks.STONE, Tags.Items.STONE);
        copy(Tags.Blocks.STORAGE_BLOCKS, Tags.Items.STORAGE_BLOCKS);
        copy(Tags.Blocks.STORAGE_BLOCKS_COAL, Tags.Items.STORAGE_BLOCKS_COAL);
        copy(Tags.Blocks.STORAGE_BLOCKS_DIAMOND, Tags.Items.STORAGE_BLOCKS_DIAMOND);
        copy(Tags.Blocks.STORAGE_BLOCKS_EMERALD, Tags.Items.STORAGE_BLOCKS_EMERALD);
        copy(Tags.Blocks.STORAGE_BLOCKS_GOLD, Tags.Items.STORAGE_BLOCKS_GOLD);
        copy(Tags.Blocks.STORAGE_BLOCKS_IRON, Tags.Items.STORAGE_BLOCKS_IRON);
        copy(Tags.Blocks.STORAGE_BLOCKS_LAPIS, Tags.Items.STORAGE_BLOCKS_LAPIS);
        copy(Tags.Blocks.STORAGE_BLOCKS_QUARTZ, Tags.Items.STORAGE_BLOCKS_QUARTZ);
        copy(Tags.Blocks.STORAGE_BLOCKS_REDSTONE, Tags.Items.STORAGE_BLOCKS_REDSTONE);
        getOrCreateTagBuilder(Tags.Items.STRING).add(Items.field_8276);
    }

    private void addColored(Consumer<Tag<Item>> consumer, Tag<Item> group, String pattern)
    {
        String prefix = group.getId().getPath().toUpperCase(Locale.ENGLISH) + '_';
        for (DyeColor color  : DyeColor.values())
        {
            Identifier key = new Identifier("minecraft", pattern.replace("{color}",  color.getName()));
            Tag<Item> tag = getForgeItemTag(prefix + color.getName());
            Item item = ForgeRegistries.ITEMS.getValue(key);
            if (item == null || item  == Items.AIR)
                throw new IllegalStateException("Unknown vanilla item: " + key.toString());
            getOrCreateTagBuilder(tag).add(item);
            consumer.accept(tag);
        }
    }

    private void copyColored(Tag<Block> blockGroup, Tag<Item> itemGroup)
    {
        String blockPre = blockGroup.getId().getPath().toUpperCase(Locale.ENGLISH) + '_';
        String itemPre = itemGroup.getId().getPath().toUpperCase(Locale.ENGLISH) + '_';
        for (DyeColor color  : DyeColor.values())
        {
            Tag<Block> from = getForgeBlockTag(blockPre + color.getName());
            Tag<Item> to = getForgeItemTag(itemPre + color.getName());
            copy(from, to);
        }
        copy(getForgeBlockTag(blockPre + "colorless"), getForgeItemTag(itemPre + "colorless"));
    }

    @SuppressWarnings("unchecked")
    private Tag<Block> getForgeBlockTag(String name)
    {
        try
        {
            name = name.toUpperCase(Locale.ENGLISH);
            return (Tag<Block>)Tags.Blocks.class.getDeclaredField(name).get(null);
        }
        catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e)
        {
            throw new IllegalStateException(Tags.Blocks.class.getName() + " is missing tag name: " + name);
        }
    }

    @SuppressWarnings("unchecked")
    private Tag<Item> getForgeItemTag(String name)
    {
        try
        {
            name = name.toUpperCase(Locale.ENGLISH);
            return (Tag<Item>)Tags.Items.class.getDeclaredField(name).get(null);
        }
        catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e)
        {
            throw new IllegalStateException(Tags.Items.class.getName() + " is missing tag name: " + name);
        }
    }

    @Override
    protected Path getOutput(Identifier id)
    {
        return filter != null && filter.contains(id) ? null : super.getOutput(id); //We don't want to save vanilla tags.
    }

    @Override
    public String getName()
    {
        return "Forge Item Tags";
    }
}
