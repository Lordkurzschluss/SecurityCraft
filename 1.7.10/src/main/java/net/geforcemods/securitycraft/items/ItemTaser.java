package net.geforcemods.securitycraft.items;

import net.geforcemods.securitycraft.SecurityCraft;
import net.geforcemods.securitycraft.entity.EntityTaserBullet;
import net.geforcemods.securitycraft.misc.SCSounds;
import net.geforcemods.securitycraft.network.packets.PacketCPlaySoundAtPos;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ItemTaser extends Item {

	public ItemTaser(){
		super();
		setMaxDurability(151);
	}

	@Override
	public boolean isFull3D(){
		return true;
	}

	@Override
	public ItemStack onItemRightClick(ItemStack par1ItemStack, World par2World, EntityPlayer par3EntityPlayer){
		if(!par2World.isRemote)
			if(!par1ItemStack.isItemDamaged()){
				par2World.spawnEntityInWorld(new EntityTaserBullet(par2World, par3EntityPlayer));
				SecurityCraft.network.sendToAll(new PacketCPlaySoundAtPos(par3EntityPlayer.posX, par3EntityPlayer.posY, par3EntityPlayer.posZ, SCSounds.TASERFIRED.path, 1.0F));

				if(!par3EntityPlayer.capabilities.isCreativeMode)
					par1ItemStack.damageItem(150, par3EntityPlayer);
			}

		return par1ItemStack;
	}

	@Override
	public void onUpdate(ItemStack par1ItemStack, World par2World, Entity par3Entity, int par4, boolean par5){
		if(!par2World.isRemote)
			if(par1ItemStack.getMetadata() >= 1)
				par1ItemStack.setMetadata(par1ItemStack.getMetadata() - 1);
	}

}
