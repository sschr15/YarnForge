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

package net.minecraftforge.common.extensions;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Function;

import javax.annotation.Nonnull;

import com.google.gson.JsonArray;
import net.minecraft.tag.Tag;
import net.minecraft.tag.TagContainer;
import net.minecraft.util.Identifier;
import net.minecraftforge.common.data.IOptionalTagEntry;

public interface IForgeTagBuilder<T>
{

    default Tag.Builder<T> addOptional(final TagContainer<T> collection, final Identifier... locations)
    {
        return addOptional(collection, Arrays.asList(locations));
    }

    default Tag.Builder<T> addOptional(final TagContainer<T> collection, final Collection<Identifier> locations)
    {
        return addOptional(collection.getEntryLookup(), locations);
    }

    @Deprecated //Use the TagCollection version
    default Tag.Builder<T> addOptional(final Function<Identifier, Optional<T>> entryLookup, final Collection<Identifier> locations)
    {
        return ((Tag.Builder<T>)this).add(new IOptionalTagEntry<T>() {
            @Override
            public void build(Collection<T> itemsIn)
            {
                locations.stream().map(entryLookup).forEach(e -> e.ifPresent(itemsIn::add));
            }

            @Override
            public void toJson(JsonArray array, Function<T, Identifier> getNameForObject)
            {
                locations.stream().map(Identifier::toString).forEach(array::add);
            }
        });
    }

    default Tag.Builder<T> addOptionalTag(final TagContainer<T> collection, @SuppressWarnings("unchecked") final Tag<T>... tags)
    {
        for (Tag<T> tag : tags)
            addOptionalTag(tag.getId());
        return ((Tag.Builder<T>)this);
    }

    default Tag.Builder<T> addOptionalTag(final Identifier... tags)
    {
        for (Identifier rl : tags)
            addOptionalTag(rl);
        return ((Tag.Builder<T>)this);
    }

    default Tag.Builder<T> addOptionalTag(Identifier tag)
    {
        class TagTarget<U> extends Tag.TagEntry<U> implements IOptionalTagEntry<U>
        {
            private Tag<U> resolvedTag = null;
            protected TagTarget(Identifier referent) {
                super(referent);
            }

            @Override
            public boolean applyTagGetter(@Nonnull Function<Identifier, Tag<U>> resolver)
            {
                if (this.resolvedTag == null)
                    this.resolvedTag = resolver.apply(this.getId());
                return true; // never fail if resolver returns null
            }

            @Override
            public void build(@Nonnull Collection<U> items)
            {
                if (this.resolvedTag != null)
                    items.addAll(this.resolvedTag.values());
            }
        };

        return ((Tag.Builder<T>)this).add(new TagTarget<>(tag));
    }
}
