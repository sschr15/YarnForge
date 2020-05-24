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

package net.minecraftforge.client.model;

import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

import javax.annotation.Nullable;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormatElement;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.ModelBakeSettings;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.render.model.json.ModelItemPropertyOverrideList;
import net.minecraft.client.render.model.json.ModelTransformation.Mode;
import net.minecraft.client.renderer.model.*;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Rotation3;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.geometry.IModelGeometry;
import net.minecraftforge.client.model.pipeline.BakedQuadBuilder;
import net.minecraftforge.client.model.pipeline.IVertexConsumer;
import net.minecraftforge.client.model.pipeline.TRSRTransformer;
import net.minecraftforge.fluids.FluidAttributes;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public final class FluidModel implements IModelGeometry<FluidModel>
{
    public static final FluidModel WATER = new FluidModel(Fluids.WATER);
    public static final FluidModel LAVA = new FluidModel(Fluids.LAVA);

    private final Fluid fluid;

    public FluidModel(Fluid fluid)
    {
        this.fluid = fluid;
    }

    @Override
    public Collection<SpriteIdentifier> getTextures(IModelConfiguration owner, Function<Identifier, UnbakedModel> modelGetter, Set<com.mojang.datafixers.util.Pair<String, String>> missingTextureErrors)
    {
        return ForgeHooksClient.getFluidMaterials(fluid).collect(Collectors.toList());
    }

    @Override
    public BakedModel bake(IModelConfiguration owner, ModelLoader bakery, Function<SpriteIdentifier, Sprite> spriteGetter, ModelBakeSettings modelTransform, ModelItemPropertyOverrideList overrides, Identifier modelLocation)
    {
        FluidAttributes attrs = fluid.getAttributes();
        return new CachingBakedFluid(
                modelTransform.getRotation(),
                PerspectiveMapWrapper.getTransforms(modelTransform),
                modelLocation,
                attrs.getColor(),
                spriteGetter.apply(ForgeHooksClient.getBlockMaterial(attrs.getStillTexture())),
                spriteGetter.apply(ForgeHooksClient.getBlockMaterial(attrs.getFlowingTexture())),
                Optional.ofNullable(attrs.getOverlayTexture()).map(ForgeHooksClient::getBlockMaterial).map(spriteGetter),
                attrs.isLighterThanAir(),
                null
        );
    }

    private static final class CachingBakedFluid extends BakedFluid
    {
        private final LoadingCache<Long, BakedFluid> modelCache = CacheBuilder.newBuilder().maximumSize(200).build(new CacheLoader<Long, BakedFluid>()
        {
            @Override
            public BakedFluid load(Long key)
            {
                boolean statePresent = (key & 1) != 0;
                key >>>= 1;
                int[] cornerRound = new int[4];
                for (int i = 0; i < 4; i++)
                {
                    cornerRound[i] = (int) (key & 0x3FF);
                    key >>>= 10;
                }
                int flowRound = (int) (key & 0x7FF) - 1024;
                key >>>= 11;
                boolean[] overlaySides = new boolean[4];
                for (int i = 0; i < 4; i++)
                {
                    overlaySides[i] = (key & 1) != 0;
                    key >>>= 1;
                }
                return new BakedFluid(transformation, transforms, modelLocation, color, still, flowing, overlay, gas, statePresent, cornerRound, flowRound, overlaySides);
            }
        });

        public CachingBakedFluid(Rotation3 transformation, ImmutableMap<Mode, Rotation3> transforms, Identifier modelLocation, int color, Sprite still, Sprite flowing, Optional<Sprite> overlay, boolean gas, Optional<IModelData> stateOption)
        {
            super(transformation, transforms, modelLocation, color, still, flowing, overlay, gas, stateOption.isPresent(), getCorners(stateOption), getFlow(stateOption), getOverlay(stateOption));
        }

        /**
         * Gets the quantized fluid levels for each corner.
         *
         * Each value is packed into 10 bits of the model key, so max range is [0,1024).
         * The value is currently stored/interpreted as the closest multiple of 1/864.
         * The divisor is chosen here to allows likely flow values to be exactly representable
         * while also providing good use of the available value range.
         * (For fluids with default quanta, this evenly divides the per-block intervals of 1/9 by 96)
         */
        private static int[] getCorners(Optional<IModelData> stateOption)
        {
            int[] cornerRound = {0, 0, 0, 0};
            if (stateOption.isPresent())
            {
                IModelData state = stateOption.get();
                for (int i = 0; i < 4; i++)
                {
                    Float level = null; // TODO fluids state.getValue(BlockFluidBase.LEVEL_CORNERS[i]);
                    cornerRound[i] = Math.round((level == null ? 8f / 9f : level) * 864);
                }
            }
            return cornerRound;
        }

        /**
         * Gets the quantized flow direction of the fluid.
         *
         * This value comprises 11 bits of the model key, and is signed, so the max range is [-1024,1024).
         * The value is currently stored as the angle rounded to the nearest degree.
         * A value of -1000 is used to signify no flow.
         */
        private static int getFlow(Optional<IModelData> stateOption)
        {
            Float flow = -1000f;
            if (stateOption.isPresent())
            {
                flow = null; // TODO fluids stateOption.get().getValue(BlockFluidBase.FLOW_DIRECTION);
                if (flow == null) flow = -1000f;
            }
            int flowRound = (int) Math.round(Math.toDegrees(flow));
            flowRound = MathHelper.clamp(flowRound, -1000, 1000);
            return flowRound;
        }

        /**
         * Gets the overlay texture flag for each side.
         *
         * This value determines if the fluid "overlay" texture should be used for that side,
         * instead of the normal "flowing" texture (if applicable for that fluid).
         * The sides are stored here by their regular horizontal index.
         */
        private static boolean[] getOverlay(Optional<IModelData> stateOption)
        {
            boolean[] overlaySides = new boolean[4];
            if (stateOption.isPresent())
            {
                IModelData state = stateOption.get();
                for (int i = 0; i < 4; i++)
                {
                    Boolean overlay = null; // TODO fluids state.getValue(BlockFluidBase.SIDE_OVERLAYS[i]);
                    if (overlay != null) overlaySides[i] = overlay;
                }
            }
            return overlaySides;
        }

        @Override
        public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, Random rand, IModelData modelData)
        {
            if (side != null)
            {
                Optional<IModelData> exState = Optional.of(modelData);

                int[] cornerRound = getCorners(exState);
                int flowRound = getFlow(exState);
                boolean[] overlaySides = getOverlay(exState);

                long key = 0L;
                for (int i = 3; i >= 0; i--)
                {
                    key <<= 1;
                    key |= overlaySides[i] ? 1 : 0;
                }
                key <<= 11;
                key |= flowRound + 1024;
                for (int i = 3; i >= 0; i--)
                {
                    key <<= 10;
                    key |= cornerRound[i];
                }
                key <<= 1;
                key |= 1;

                return modelCache.getUnchecked(key).getQuads(state, side, rand);
            }

            return super.getQuads(state, side, rand);
        }
    }

    private static class BakedFluid implements BakedModel
    {
        private static final int x[] = { 0, 0, 1, 1 };
        private static final int z[] = { 0, 1, 1, 0 };
        private static final float eps = 1e-3f;

        protected final Rotation3 transformation;
        protected final ImmutableMap<Mode, Rotation3> transforms;
        protected final Identifier modelLocation;
        protected final int color;
        protected final Sprite still, flowing;
        protected final Optional<Sprite> overlay;
        protected final boolean gas;
        protected final ImmutableMap<Direction, ImmutableList<BakedQuad>> faceQuads;

        public BakedFluid(Rotation3 transformation, ImmutableMap<Mode, Rotation3> transforms, Identifier modelLocation, int color, Sprite still, Sprite flowing, Optional<Sprite> overlay, boolean gas, boolean statePresent, int[] cornerRound, int flowRound, boolean[] sideOverlays)
        {
            this.transformation = transformation;
            this.transforms = transforms;
            this.modelLocation = modelLocation;
            this.color = color;
            this.still = still;
            this.flowing = flowing;
            this.overlay = overlay;
            this.gas = gas;
            this.faceQuads = buildQuads(statePresent, cornerRound, flowRound, sideOverlays);
        }

        private ImmutableMap<Direction, ImmutableList<BakedQuad>> buildQuads(boolean statePresent, int[] cornerRound, int flowRound, boolean[] sideOverlays)
        {
            EnumMap<Direction, ImmutableList<BakedQuad>> faceQuads = new EnumMap<>(Direction.class);
            for (Direction side : Direction.values())
            {
                faceQuads.put(side, ImmutableList.of());
            }

            if (statePresent)
            {
                // y levels
                float[] y = new float[4];
                boolean fullVolume = true;
                for (int i = 0; i < 4; i++)
                {
                    float value = cornerRound[i] / 864f;
                    if (value < 1f) fullVolume = false;
                    y[i] = gas ? 1f - value : value;
                }

                // flow
                boolean isFlowing = flowRound > -1000;

                float flow = isFlowing ? (float) Math.toRadians(flowRound) : 0f;
                Sprite topSprite = isFlowing ? flowing : still;
                float scale = isFlowing ? 4f : 8f;

                float c = MathHelper.cos(flow) * scale;
                float s = MathHelper.sin(flow) * scale;

                // top
                Direction top = gas ? Direction.field_11033 : Direction.field_11036;

                // base uv offset for flow direction
                VertexParameter uv = i -> c * (x[i] * 2 - 1) + s * (z[i] * 2 - 1);

                VertexParameter topX = i -> x[i];
                VertexParameter topY = i -> y[i];
                VertexParameter topZ = i -> z[i];
                VertexParameter topU = i -> 8 + uv.get(i);
                VertexParameter topV = i -> 8 + uv.get((i + 1) % 4);

                {
                    ImmutableList.Builder<BakedQuad> builder = ImmutableList.builder();

                    builder.add(buildQuad(top, topSprite, gas, false, topX, topY, topZ, topU, topV));
                    if (!fullVolume) builder.add(buildQuad(top, topSprite, !gas, true, topX, topY, topZ, topU, topV));

                    faceQuads.put(top, builder.build());
                }

                // bottom
                Direction bottom = top.getOpposite();
                faceQuads.put(bottom, ImmutableList.of(
                        buildQuad(bottom, still, gas, false,
                                i -> z[i],
                                i -> gas ? 1 : 0,
                                i -> x[i],
                                i -> z[i] * 16,
                                i -> x[i] * 16
                        )
                ));

                // sides
                for (int i = 0; i < 4; i++)
                {
                    Direction side = Direction.fromHorizontal((5 - i) % 4); // [W, S, E, N]
                    boolean useOverlay = overlay.isPresent() && sideOverlays[side.getHorizontal()];
                    int si = i; // local var for lambda capture

                    VertexParameter sideX = j -> x[(si + x[j]) % 4];
                    VertexParameter sideY = j -> z[j] == 0 ? (gas ? 1 : 0) : y[(si + x[j]) % 4];
                    VertexParameter sideZ = j -> z[(si + x[j]) % 4];
                    VertexParameter sideU = j -> x[j] * 8;
                    VertexParameter sideV = j -> (gas ? sideY.get(j) : 1 - sideY.get(j)) * 8;

                    ImmutableList.Builder<BakedQuad> builder = ImmutableList.builder();

                    if (!useOverlay) builder.add(buildQuad(side, flowing, gas, true, sideX, sideY, sideZ, sideU, sideV));
                    builder.add(buildQuad(side, useOverlay ? overlay.get() : flowing, !gas, false, sideX, sideY, sideZ, sideU, sideV));

                    faceQuads.put(side, builder.build());
                }
            }
            else
            {
                // inventory
                faceQuads.put(Direction.field_11035, ImmutableList.of(
                        buildQuad(Direction.field_11036, still, false, false,
                                i -> z[i],
                                i -> x[i],
                                i -> 0,
                                i -> z[i] * 16,
                                i -> x[i] * 16
                        )
                ));
            }

            return ImmutableMap.copyOf(faceQuads);
        }

        // maps vertex index to parameter value
        private interface VertexParameter
        {
            float get(int index);
        }

        private BakedQuad buildQuad(Direction side, Sprite texture, boolean flip, boolean offset, VertexParameter x, VertexParameter y, VertexParameter z, VertexParameter u, VertexParameter v)
        {
            BakedQuadBuilder builder = new BakedQuadBuilder(texture);

            builder.setQuadOrientation(side);
            builder.setQuadTint(0);

            boolean hasTransform = !transformation.isIdentity();
            IVertexConsumer consumer = hasTransform ? new TRSRTransformer(builder, transformation) : builder;

            for (int i = 0; i < 4; i++)
            {
                int vertex = flip ? 3 - i : i;
                putVertex(
                    consumer, side, offset,
                    x.get(vertex), y.get(vertex), z.get(vertex),
                    texture.getFrameU(u.get(vertex)),
                    texture.getFrameV(v.get(vertex))
                );
            }

            return builder.build();
        }

        private void putVertex(IVertexConsumer consumer, Direction side, boolean offset, float x, float y, float z, float u, float v)
        {
            VertexFormat format = VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL;
            ImmutableList<VertexFormatElement> elements = format.getElements();
            for(int e = 0; e < elements.size(); e++)
            {
                switch(elements.get(e).getType())
                {
                case field_1633:
                    float dx = offset ? side.getVector().getX() * eps : 0f;
                    float dy = offset ? side.getVector().getY() * eps : 0f;
                    float dz = offset ? side.getVector().getZ() * eps : 0f;
                    consumer.put(e, x - dx, y - dy, z - dz, 1f);
                    break;
                case field_1632:
                    float r = ((color >> 16) & 0xFF) / 255f;
                    float g = ((color >>  8) & 0xFF) / 255f;
                    float b = ( color        & 0xFF) / 255f;
                    float a = ((color >> 24) & 0xFF) / 255f;
                    consumer.put(e, r, g, b, a);
                    break;
                case field_1635:
                    float offX = (float) side.getOffsetX();
                    float offY = (float) side.getOffsetY();
                    float offZ = (float) side.getOffsetZ();
                    consumer.put(e, offX, offY, offZ, 0f);
                    break;
                case field_1636:
                    if(elements.get(e).getIndex() == 0)
                    {
                        consumer.put(e, u, v, 0f, 1f);
                        break;
                    }
                    // else fallthrough to default
                default:
                    consumer.put(e);
                    break;
                }
            }
        }

        @Override
        public boolean useAmbientOcclusion()
        {
            return true;
        }

        @Override
        public boolean hasDepth()
        {
            return false;
        }

        @Override
        public boolean isSideLit()
        {
            return false;
        }

        @Override
        public boolean isBuiltin()
        {
            return false;
        }

        @Override
        public Sprite getSprite()
        {
            return still;
        }

        @Override
        public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, Random rand)
        {
            return side == null ? ImmutableList.of() : faceQuads.get(side);
        }

        @Override
        public ModelItemPropertyOverrideList getItemPropertyOverrides()
        {
            return ModelItemPropertyOverrideList.EMPTY;
        }

        @Override
        public boolean doesHandlePerspectives()
        {
            return true;
        }

        @Override
        public BakedModel handlePerspective(Mode type, MatrixStack mat)
        {
            return PerspectiveMapWrapper.handlePerspective(this, transforms, type, mat);
        }
    }
}
