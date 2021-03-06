package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.common.tileentity.ICamouflageableTE;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.common.util.PropertyObject;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Base class for blocks which may be camouflaged, storing the camouflaged block state in the
 * CAMO_STATE unlisted property
 */
public abstract class BlockPneumaticCraftCamo extends BlockPneumaticCraftModeled {
    public static final PropertyObject<IBlockState> CAMO_STATE = new PropertyObject<>("camo_state", IBlockState.class);

    protected BlockPneumaticCraftCamo(Material par2Material, String registryName) {
        super(par2Material, registryName);
    }

    /**
     * When camouflaged, should getBoundingBox() return the bounding box of the camo block?  Override this
     * to return false if the subclass needs to be able to highlight subsections, e.g. elevator caller buttons
     *
     * @return true if camouflage block's bounding box should always be used
     */
    protected boolean doesCamoOverrideBounds() {
        return true;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return isRotatable() ?
                new ExtendedBlockState(this, new IProperty[] { ROTATION } , new IUnlistedProperty[] { CAMO_STATE }) :
                new ExtendedBlockState(this, new IProperty[] { } , new IUnlistedProperty[] { CAMO_STATE });
    }

    @Override
    public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
        TileEntity te = PneumaticCraftUtils.getTileEntitySafely(world, pos);
        if (te instanceof ICamouflageableTE) {
            IBlockState camoState = ((ICamouflageableTE) te).getCamouflage();
            if (camoState != null) {
                return ((IExtendedBlockState) state).withProperty(CAMO_STATE, camoState);
            }
        }
        return state;
    }

    @Override
    public BlockRenderLayer getBlockLayer() {
        return BlockRenderLayer.CUTOUT_MIPPED;
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        IBlockState camo = getCamoState(source, pos);
        return camo != null && doesCamoOverrideBounds() ? camo.getBoundingBox(source, pos) : super.getBoundingBox(state, source, pos);
    }

    @Nullable
    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
        IBlockState camo = getCamoState(worldIn, pos);
        return camo != null ? camo.getCollisionBoundingBox(worldIn, pos) : super.getCollisionBoundingBox(blockState, worldIn, pos);
    }

    @Override
    public void addCollisionBoxToList(IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, @Nullable Entity entityIn, boolean p_185477_7_) {
        IBlockState camo = getCamoState(worldIn, pos);
        if (camo != null) {
            addCollisionBoxToList(pos, entityBox, collidingBoxes, camo.getBoundingBox(worldIn, pos));
        } else {
            super.addCollisionBoxToList(state, worldIn, pos, entityBox, collidingBoxes, entityIn, p_185477_7_);
        }
    }

    @Override
    public boolean doesSideBlockRendering(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing face) {
        IBlockState camo = getCamoState(world, pos);
        return camo == null ? super.doesSideBlockRendering(state, world, pos, face) : camo.doesSideBlockRendering(world, pos, face);
    }

    @Override
    public boolean isSideSolid(IBlockState base_state, IBlockAccess world, BlockPos pos, EnumFacing side) {
        // ensure levers etc. can be attached to the block even though it can possibly emit redstone
        IBlockState camo = getCamoState(world, pos);
        return camo == null ? super.isSideSolid(base_state, world, pos, side) : camo.isSideSolid(world, pos, side);
    }

//    @Override
//    public boolean shouldSideBeRendered(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
//        IBlockState camo = getCamoState(blockAccess, pos);
//        return camo == null ? super.shouldSideBeRendered(blockState, blockAccess, pos, side) : camo.shouldSideBeRendered(blockAccess, pos, side);
//    }

    protected IBlockState getCamoState(IBlockAccess blockAccess, BlockPos pos) {
        TileEntity te = PneumaticCraftUtils.getTileEntitySafely(blockAccess, pos);
        return te instanceof ICamouflageableTE ? ((ICamouflageableTE) te).getCamouflage() : null;
    }

    @Override
    public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer) {
        return true;
    }
}
