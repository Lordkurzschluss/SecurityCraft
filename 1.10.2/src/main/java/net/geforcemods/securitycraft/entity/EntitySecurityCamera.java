package net.geforcemods.securitycraft.entity;

import org.lwjgl.input.Mouse;

import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.SecurityCraft;
import net.geforcemods.securitycraft.api.CustomizableSCTE;
import net.geforcemods.securitycraft.blocks.BlockSecurityCamera;
import net.geforcemods.securitycraft.misc.EnumCustomModules;
import net.geforcemods.securitycraft.misc.KeyBindings;
import net.geforcemods.securitycraft.misc.SCSounds;
import net.geforcemods.securitycraft.network.packets.PacketCSetPlayerPositionAndRotation;
import net.geforcemods.securitycraft.network.packets.PacketGivePotionEffect;
import net.geforcemods.securitycraft.network.packets.PacketSSetCameraRotation;
import net.geforcemods.securitycraft.network.packets.PacketSetBlock;
import net.geforcemods.securitycraft.util.BlockUtils;
import net.geforcemods.securitycraft.util.ClientUtils;
import net.geforcemods.securitycraft.util.PlayerUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class EntitySecurityCamera extends Entity{

	private final float CAMERA_SPEED = SecurityCraft.config.cameraSpeed;

	public int blockPosX;
	public int blockPosY;
	public int blockPosZ;

	private double cameraUseX;
	private double cameraUseY;
	private double cameraUseZ;
	private float cameraUseYaw;
	private float cameraUsePitch;

	private int id;
	private int screenshotCooldown = 0;
	private int redstoneCooldown = 0;
	private int toggleNightVisionCooldown = 0;
	private int toggleLightCooldown = 0;
	private boolean shouldProvideNightVision = false;
	private float zoomAmount = 1F;

	private String playerViewingName = null;

	public EntitySecurityCamera(World world){
		super(world);
		noClip = true;
		height = 0.0001F;
		width = 0.0001F;
	}

	public EntitySecurityCamera(World world, double x, double y, double z, int id, EntityPlayer player){
		this(world);
		blockPosX = (int) x;
		blockPosY = (int) y;
		blockPosZ = (int) z;
		cameraUseX = player.posX;
		cameraUseY = player.posY;
		cameraUseZ = player.posZ;
		cameraUseYaw = player.rotationYaw;
		cameraUsePitch = player.rotationPitch;
		this.id = id;
		playerViewingName = player.getName();
		setPosition(x + 0.5D, y + 1, z + 0.5D);

		rotationPitch = 30F;

		EnumFacing facing = BlockUtils.getBlockPropertyAsEnum(worldObj, BlockUtils.toPos((int) Math.floor(posX), (int) (posY - 1D), (int) Math.floor(posZ)), BlockSecurityCamera.FACING);

		if(facing == EnumFacing.NORTH)
			rotationYaw = 180F;
		else if(facing == EnumFacing.WEST)
			rotationYaw = 90F;
		else if(facing == EnumFacing.SOUTH)
			rotationYaw = 0F;
		else if(facing == EnumFacing.EAST)
			rotationYaw = 270F;
	}

	public EntitySecurityCamera(World world, double x, double y, double z, int id, EntitySecurityCamera camera){
		this(world);
		blockPosX = (int) x;
		blockPosY = (int) y;
		blockPosZ = (int) z;
		cameraUseX = camera.cameraUseX;
		cameraUseY = camera.cameraUseY;
		cameraUseZ = camera.cameraUseZ;
		cameraUseYaw = camera.cameraUseYaw;
		cameraUsePitch = camera.cameraUsePitch;
		this.id = id;
		playerViewingName = camera.playerViewingName;
		setPosition(x + 0.5D, y + 1.0D, z + 0.5D);

		rotationPitch = 30.0F;

		EnumFacing facing = BlockUtils.getBlockPropertyAsEnum(worldObj, BlockUtils.toPos((int) Math.floor(posX), (int) (posY - 1D), (int) Math.floor(posZ)), BlockSecurityCamera.FACING);

		if(facing == EnumFacing.NORTH)
			rotationYaw = 180F;
		else if(facing == EnumFacing.WEST)
			rotationYaw = 90F;
		else if(facing == EnumFacing.SOUTH)
			rotationYaw = 0F;
		else if(facing == EnumFacing.EAST)
			rotationYaw = 270F;
	}

	@Override
	public double getMountedYOffset(){
		return height * -7500D;
	}

	@Override
	protected boolean shouldSetPosAfterLoading(){
		return false;
	}

	@Override
	public boolean shouldDismountInWater(Entity rider){
		return true;
	}

	@Override
	public void onUpdate(){
		if(worldObj.isRemote && isBeingRidden()){
			EntityPlayer lowestEntity = (EntityPlayer)getPassengers().get(0);

			if(screenshotCooldown > 0)
				screenshotCooldown -= 1;

			if(redstoneCooldown > 0)
				redstoneCooldown -= 1;

			if(toggleNightVisionCooldown > 0)
				toggleNightVisionCooldown -= 1;

			if(toggleLightCooldown > 0)
				toggleLightCooldown -= 1;

			if(lowestEntity.rotationYaw != rotationYaw){
				lowestEntity.setPositionAndRotation(lowestEntity.posX, lowestEntity.posY, lowestEntity.posZ, rotationYaw, rotationPitch);
				lowestEntity.rotationYaw = rotationYaw;
			}

			if(lowestEntity.rotationPitch != rotationPitch)
				lowestEntity.setPositionAndRotation(lowestEntity.posX, lowestEntity.posY, lowestEntity.posZ, rotationYaw, rotationPitch);

			checkKeysPressed();

			if(Mouse.hasWheel() && Mouse.isButtonDown(2) && screenshotCooldown == 0){
				screenshotCooldown = 30;
				ClientUtils.takeScreenshot();
				Minecraft.getMinecraft().theWorld.playSound(new BlockPos(posX, posY, posZ), SoundEvent.REGISTRY.getObject(SCSounds.CAMERASNAP.location), SoundCategory.BLOCKS, 1.0F, 1.0F, true);
			}

			if(getPassengers().size() != 0 && shouldProvideNightVision)
				SecurityCraft.network.sendToServer(new PacketGivePotionEffect(Potion.getIdFromPotion(Potion.getPotionFromResourceLocation("night_vision")), 3, -1));
		}

		if(!worldObj.isRemote)
			if(getPassengers().size() == 0 | BlockUtils.getBlock(worldObj, blockPosX, blockPosY, blockPosZ) != SCContent.securityCamera){
				setDead();
				return;
			}
	}

	@SideOnly(Side.CLIENT)
	private void checkKeysPressed() {
		if (Minecraft.getMinecraft().gameSettings.keyBindSneak.isPressed())
			dismountRidingEntity();

		if(Minecraft.getMinecraft().gameSettings.keyBindForward.isKeyDown())
			moveViewUp();

		if(Minecraft.getMinecraft().gameSettings.keyBindBack.isKeyDown())
			moveViewDown();

		if(Minecraft.getMinecraft().gameSettings.keyBindLeft.isKeyDown())
			moveViewLeft();

		if(Minecraft.getMinecraft().gameSettings.keyBindRight.isKeyDown())
			moveViewRight();

		if(KeyBindings.cameraEmitRedstone.isPressed() && redstoneCooldown == 0){
			setRedstonePower();
			redstoneCooldown = 30;
		}

		if(KeyBindings.cameraActivateNightVision.isPressed() && toggleNightVisionCooldown == 0)
			enableNightVision();

		if(KeyBindings.cameraZoomIn.isPressed())
			zoomCameraView(-1);

		if(KeyBindings.cameraZoomOut.isPressed())
			zoomCameraView(1);
	}

	public void moveViewUp() {
		if(rotationPitch > -25F)
			setRotation(rotationYaw, rotationPitch -= CAMERA_SPEED);

		updateServerRotation();
	}

	public void moveViewDown(){
		if(rotationPitch < 60F)
			setRotation(rotationYaw, rotationPitch += CAMERA_SPEED);

		updateServerRotation();
	}

	public void moveViewLeft() {
		if(BlockUtils.hasBlockProperty(worldObj, BlockUtils.toPos((int) Math.floor(posX), (int) (posY - 1D), (int) Math.floor(posZ)), BlockSecurityCamera.FACING)) {
			EnumFacing facing = BlockUtils.getBlockPropertyAsEnum(worldObj, BlockUtils.toPos((int) Math.floor(posX), (int) (posY - 1D), (int) Math.floor(posZ)), BlockSecurityCamera.FACING);

			if(facing == EnumFacing.EAST){
				if((rotationYaw - CAMERA_SPEED) > -180F)
					setRotation(rotationYaw -= CAMERA_SPEED, rotationPitch);
			}else if(facing == EnumFacing.WEST){
				if((rotationYaw - CAMERA_SPEED) > 0F)
					setRotation(rotationYaw -= CAMERA_SPEED, rotationPitch);
			}else if(facing == EnumFacing.NORTH){
				// Handles some problems the occurs from the way the rotationYaw value works in MC
				if((((rotationYaw - CAMERA_SPEED) > 90F) && ((rotationYaw - CAMERA_SPEED) < 185F)) || (((rotationYaw - CAMERA_SPEED) > -190F) && ((rotationYaw - CAMERA_SPEED) < -90F)))
					setRotation(rotationYaw -= CAMERA_SPEED, rotationPitch);
			}else if(facing == EnumFacing.SOUTH)
				if((rotationYaw - CAMERA_SPEED) > -90F)
					setRotation(rotationYaw -= CAMERA_SPEED, rotationPitch);

			updateServerRotation();
		}
	}

	public void moveViewRight(){
		if(BlockUtils.hasBlockProperty(worldObj, BlockUtils.toPos((int) Math.floor(posX), (int) (posY - 1D), (int) Math.floor(posZ)), BlockSecurityCamera.FACING)) {
			EnumFacing facing = BlockUtils.getBlockPropertyAsEnum(worldObj, BlockUtils.toPos((int) Math.floor(posX), (int) (posY - 1D), (int) Math.floor(posZ)), BlockSecurityCamera.FACING);

			if(facing == EnumFacing.EAST){
				if((rotationYaw + CAMERA_SPEED) < 0F)
					setRotation(rotationYaw += CAMERA_SPEED, rotationPitch);
			}else if(facing == EnumFacing.WEST){
				if((rotationYaw + CAMERA_SPEED) < 180F)
					setRotation(rotationYaw += CAMERA_SPEED, rotationPitch);
			}else if(facing == EnumFacing.NORTH){
				if((((rotationYaw + CAMERA_SPEED) > 85F) && ((rotationYaw + CAMERA_SPEED) < 185F)) || ((rotationYaw + CAMERA_SPEED) < -95F) && ((rotationYaw + CAMERA_SPEED) > -180F))
					setRotation(rotationYaw += CAMERA_SPEED, rotationPitch);
			}else if(facing == EnumFacing.SOUTH)
				if((rotationYaw + CAMERA_SPEED) < 90F)
					setRotation(rotationYaw += CAMERA_SPEED, rotationPitch);

			updateServerRotation();
		}
	}

	public void zoomCameraView(int zoom) {
		if(zoom > 0){
			if(zoomAmount == -0.5F)
				zoomAmount = 1F;
			else if(zoomAmount == 1F)
				zoomAmount = 2F;
		}else if(zoom < 0)
			if(zoomAmount == 2F)
				zoomAmount = 1F;
			else if(zoomAmount == 1F)
				zoomAmount = -0.5F;

		Minecraft.getMinecraft().theWorld.playSound(new BlockPos(posX, posY, posZ), SoundEvent.REGISTRY.getObject(SCSounds.CAMERAZOOMIN.location), SoundCategory.BLOCKS, 1.0F, 1.0F, true);
	}

	public void setRedstonePower() {
		BlockPos pos = BlockUtils.toPos((int) Math.floor(posX), (int) (posY - 1D), (int) Math.floor(posZ));

		if(((CustomizableSCTE) worldObj.getTileEntity(pos)).hasModule(EnumCustomModules.REDSTONE))
			if(BlockUtils.getBlockPropertyAsBoolean(worldObj, pos, BlockSecurityCamera.POWERED))
				SecurityCraft.network.sendToServer(new PacketSetBlock(pos.getX(), pos.getY(), pos.getZ(), "securitycraft:securityCamera", BlockUtils.getBlockMeta(worldObj, pos) - 6));
			else if(!BlockUtils.getBlockPropertyAsBoolean(worldObj, pos, BlockSecurityCamera.POWERED))
				SecurityCraft.network.sendToServer(new PacketSetBlock(pos.getX(), pos.getY(), pos.getZ(), "securitycraft:securityCamera", BlockUtils.getBlockMeta(worldObj, pos) + 6));
	}

	public void enableNightVision() {
		toggleNightVisionCooldown = 30;
		shouldProvideNightVision = !shouldProvideNightVision;
	}

	public String getCameraInfo(){
		String nowViewing = TextFormatting.UNDERLINE + "Now viewing camera #" + id + "\n\n";
		String pos = TextFormatting.YELLOW + "Pos: " + TextFormatting.RESET + "X: " + (int) Math.floor(posX) + " Y: " + (int) (posY - 1D) + " Z: " + (int) Math.floor(posZ) + "\n";
		String viewingFrom = (getPassengers().size() != 0 && SecurityCraft.instance.hasUsePosition(getPassengers().get(0).getName())) ? TextFormatting.YELLOW + "Viewing from: " + TextFormatting.RESET + " X: " + (int) Math.floor((Double) SecurityCraft.instance.getUsePosition(getPassengers().get(0).getName())[0]) + " Y: " + (int) Math.floor((Double) SecurityCraft.instance.getUsePosition(getPassengers().get(0).getName())[1]) + " Z: " + (int) Math.floor((Double) SecurityCraft.instance.getUsePosition(getPassengers().get(0).getName())[2]) : "";
		return nowViewing + pos + viewingFrom;
	}

	public float getZoomAmount(){
		return zoomAmount;
	}

	@SideOnly(Side.CLIENT)
	private void updateServerRotation(){
		SecurityCraft.network.sendToServer(new PacketSSetCameraRotation(rotationYaw, rotationPitch));
	}

	@Override
	public void setDead(){
		super.setDead();

		if(playerViewingName != null && PlayerUtils.isPlayerOnline(playerViewingName)){
			EntityPlayer player = PlayerUtils.getPlayerFromName(playerViewingName);
			player.setPositionAndUpdate(cameraUseX, cameraUseY, cameraUseZ);
			SecurityCraft.network.sendTo(new PacketCSetPlayerPositionAndRotation(cameraUseX, cameraUseY, cameraUseZ, cameraUseYaw, cameraUsePitch), (EntityPlayerMP) player);
		}
	}

	@Override
	protected void entityInit(){}

	@Override
	public void writeEntityToNBT(NBTTagCompound tagCompound){
		tagCompound.setInteger("CameraID", id);

		if(playerViewingName != null)
			tagCompound.setString("playerName", playerViewingName);

		if(cameraUseX != 0.0D)
			tagCompound.setDouble("cameraUseX", cameraUseX);

		if(cameraUseY != 0.0D)
			tagCompound.setDouble("cameraUseY", cameraUseY);

		if(cameraUseZ != 0.0D)
			tagCompound.setDouble("cameraUseZ", cameraUseZ);

		if(cameraUseYaw != 0.0D)
			tagCompound.setDouble("cameraUseYaw", cameraUseYaw);

		if(cameraUsePitch != 0.0D)
			tagCompound.setDouble("cameraUsePitch", cameraUsePitch);
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound tagCompound){
		id = tagCompound.getInteger("CameraID");

		if(tagCompound.hasKey("playerName"))
			playerViewingName = tagCompound.getString("playerName");

		if(tagCompound.hasKey("cameraUseX"))
			cameraUseX = tagCompound.getDouble("cameraUseX");

		if(tagCompound.hasKey("cameraUseY"))
			cameraUseY = tagCompound.getDouble("cameraUseY");

		if(tagCompound.hasKey("cameraUseZ"))
			cameraUseZ = tagCompound.getDouble("cameraUseZ");

		if(tagCompound.hasKey("cameraUseYaw"))
			cameraUseYaw = tagCompound.getFloat("cameraUseYaw");

		if(tagCompound.hasKey("cameraUsePitch"))
			cameraUsePitch = tagCompound.getFloat("cameraUsePitch");
	}

}
