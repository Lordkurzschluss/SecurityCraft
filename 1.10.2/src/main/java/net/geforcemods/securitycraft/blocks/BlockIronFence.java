package net.geforcemods.securitycraft.blocks;

import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.api.IIntersectable;
import net.geforcemods.securitycraft.misc.CustomDamageSources;
import net.geforcemods.securitycraft.tileentity.TileEntityOwnable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFence;
import net.minecraft.block.BlockFenceGate;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockIronFence extends BlockFence implements IIntersectable {

	public BlockIronFence(Material material)
	{
		super(material, MapColor.IRON);
		setSoundType(SoundType.METAL);
	}

	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		return false;
	}

	@Override
	public boolean canConnectTo(IBlockAccess par1IBlockAccess, BlockPos pos)
	{
		Block block = par1IBlockAccess.getBlockState(pos).getBlock();

		//split up oneliner to be more readable
		if(block != this && !(block instanceof BlockFenceGate) && block != SCContent.reinforcedFencegate)
		{
			if(block.getDefaultState().getMaterial().isOpaque())
				return block.getDefaultState().getMaterial() != Material.GOURD;
			else
				return false;
		}
		else
			return true;
	}

	@Override
	public void onEntityIntersected(World world, BlockPos pos, Entity entity)
	{
		//so dropped items don't get destroyed
		if(entity instanceof EntityItem)
			return;
		//owner check
		else if(entity instanceof EntityPlayer)
		{
			if(((TileEntityOwnable) world.getTileEntity(pos)).getOwner().isOwner((EntityPlayer)entity))
				return;
		}
		else if(entity instanceof EntityCreeper)
		{
			EntityCreeper creeper = (EntityCreeper)entity;
			EntityLightningBolt lightning = new EntityLightningBolt(world, pos.getX(), pos.getY(), pos.getZ(), true);

			creeper.onStruckByLightning(lightning);
			creeper.extinguish();
			return;
		}

		entity.attackEntityFrom(CustomDamageSources.electricity, 6.0F); //3 hearts per attack
	}

	@Override
	public void breakBlock(World par1World, BlockPos pos, IBlockState par3IBlockState)
	{
		super.breakBlock(par1World, pos, par3IBlockState);
		par1World.removeTileEntity(pos);
	}

	@Override
	public boolean eventReceived(IBlockState state, World worldIn, BlockPos pos, int eventID, int eventParam)
	{
		super.eventReceived(state, worldIn, pos, eventID, eventParam);
		TileEntity tileentity = worldIn.getTileEntity(pos);
		return tileentity == null ? false : tileentity.receiveClientEvent(eventID, eventParam);
	}

	@Override
	public TileEntity createNewTileEntity(World p_149915_1_, int p_149915_2_)
	{
		return new TileEntityOwnable().intersectsEntities();
	}
}