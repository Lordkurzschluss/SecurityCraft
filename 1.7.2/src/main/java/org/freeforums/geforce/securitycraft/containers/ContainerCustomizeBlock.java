package org.freeforums.geforce.securitycraft.containers;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import org.freeforums.geforce.securitycraft.items.ItemModule;
import org.freeforums.geforce.securitycraft.tileentity.CustomizableSCTE;

public class ContainerCustomizeBlock extends Container{
	
	private CustomizableSCTE tileEntity;

	public ContainerCustomizeBlock(InventoryPlayer inventory, CustomizableSCTE tileEntity) {
		this.tileEntity = tileEntity;
		
		if(tileEntity.getNumberOfCustomizableOptions() == 1){
			this.addSlotToContainer(new ModuleSlot(tileEntity, 0, 79, 20));
		}else if(tileEntity.getNumberOfCustomizableOptions() == 2){
			this.addSlotToContainer(new ModuleSlot(tileEntity, 0, 70, 20));
			this.addSlotToContainer(new ModuleSlot(tileEntity, 1, 88, 20));
		}
		
		for(int i = 0; i < 3; i++){
            for(int j = 0; j < 9; ++j){
                this.addSlotToContainer(new Slot(inventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }

        for(int i = 0; i < 9; i++){
            this.addSlotToContainer(new Slot(inventory, i, 8 + i * 18, 142));
        }
	}
	
    public ItemStack transferStackInSlot(EntityPlayer p_82846_1_, int p_82846_2_)
    {
        Slot slot = (Slot)this.inventorySlots.get(p_82846_2_);
        return slot != null ? slot.getStack() : null;
    }

	public boolean canInteractWith(EntityPlayer par1EntityPlayer) {
		return true;
	}
	
	
	public static class ModuleSlot extends Slot{
		private CustomizableSCTE tileEntity;
		public ModuleSlot(CustomizableSCTE par1IInventory, int par2, int par3, int par4) {
			super(par1IInventory, par2, par3, par4);
			this.tileEntity = par1IInventory;
		}
		
		/**
         * Check if the stack is a valid item for this slot. Always true beside for the armor slots.
         */
        public boolean isItemValid(ItemStack par1ItemStack)
        {
            if(par1ItemStack != null && par1ItemStack.getItem() instanceof ItemModule && tileEntity.getOptions().contains(((ItemModule) par1ItemStack.getItem()).getModule()) && !tileEntity.hasModule(((ItemModule) par1ItemStack.getItem()).getModule())){
            	return true;
            }else{
            	return false;
            }
        }
        
        public ItemStack getStack(){
        	return this.tileEntity.itemStacks[this.getSlotIndex()];
        }
        
        public void putStack(ItemStack p_75215_1_)
        {
            this.tileEntity.safeSetInventorySlotContents(this.getSlotIndex(), p_75215_1_);
            this.onSlotChanged();
        }
        
        /**
         * Decrease the size of the stack in slot (first int arg) by the amount of the second int arg. Returns the new
         * stack.
         */
        public ItemStack decrStackSize(int p_75209_1_)
        {
            return this.tileEntity.safeDecrStackSize(this.getSlotIndex(), p_75209_1_);
        }
	
		/**
         * Returns the maximum stack size for a given slot (usually the same as getInventoryStackLimit(), but 1 in the
         * case of armor slots)
         */
        public int getSlotStackLimit()
        {
            return 1;
        }        
	}

}
