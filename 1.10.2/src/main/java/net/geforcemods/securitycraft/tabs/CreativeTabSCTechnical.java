package net.geforcemods.securitycraft.tabs;

import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.util.ClientUtils;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class CreativeTabSCTechnical extends CreativeTabs{

	public CreativeTabSCTechnical(){
		super(getNextID(), "tabSecurityCraft");
	}


	@Override
	@SideOnly(Side.CLIENT)
	public Item getTabIconItem(){
		return Item.getItemFromBlock(SCContent.usernameLogger);
	}

	@Override
	public String getTranslatedTabLabel(){
		return "SecurityCraft: " + ClientUtils.localize("creativeTabTechnical");

	}
}
