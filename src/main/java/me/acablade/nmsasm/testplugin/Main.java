package me.acablade.nmsasm.testplugin;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import me.acablade.nmsasm.NMSAsm;
import me.acablade.nmsasm.testplugin.nms.CraftPlayer;
import me.acablade.nmsasm.testplugin.nms.CraftWorld;
import me.acablade.nmsasm.testplugin.nms.Entity;
import me.acablade.nmsasm.testplugin.nms.EntityLiving;
import me.acablade.nmsasm.testplugin.nms.EntityPlayer;
import me.acablade.nmsasm.testplugin.nms.PacketPlayOutSpawnEntityLiving;
import me.acablade.nmsasm.testplugin.nms.PlayerConnection;
import me.acablade.nmsasm.testplugin.nms.World;
import me.acablade.nmsasm.testplugin.nms.v1_17.EntityEnderDragonv1_17;
import me.acablade.nmsasm.testplugin.nms.v1_17.EntityTypesv1_17;
import me.acablade.nmsasm.testplugin.nms.v1_17.MinecraftKey;
import me.acablade.nmsasm.testplugin.nms.v1_20.EntityEnderDragonv1_20;
import me.acablade.nmsasm.testplugin.nms.v1_20.EntityTypesv1_20;
import me.acablade.nmsasm.testplugin.nms.v1_20.PacketPlayOutSpawnEntityv1_20;
import me.acablade.nmsasm.testplugin.nms.v1_8.EntityEnderDragonv1_8;

public class Main extends JavaPlugin implements Listener{
	
	
	private int version;
	
	
	@Override
	public void onEnable() {
		
//		NMSAsm.registerNMSClass(CraftPlayer.class);
//		NMSAsm.registerNMSClass(EntityPlayer.class);
//		NMSAsm.registerNMSClass(PlayerConnection.class);
//		NMSAsm.registerNMSClass(PacketPlayOutChat.class);
//		NMSAsm.registerNMSClass(IChatBaseComponent.class);
//		NMSAsm.registerNMSClass(ChatComponentText.class);
//		NMSAsm.registerNMSClass(PacketPlayOutOpenWindow.class);
//		NMSAsm.registerNMSClass(CraftLivingEntity.class);
//		NMSAsm.registerNMSClass(CraftWorld.class);
//		NMSAsm.registerNMSClass(EntityCreeper.class);
//		NMSAsm.registerNMSClass(PacketPlayOutSpawnEntityLiving.class);
//		NMSAsm.registerNMSClass(PacketPlayOutEntityTeleport.class);
//		NMSAsm.registerNMSClass(PacketPlayOutEntityRelMove.class);
		version = Integer.parseInt(Bukkit.getServer().getClass().getCanonicalName().split("\\.")[3].split("_")[1]);
		handleNMS(version);
		
		Bukkit.getPluginManager().registerEvents(this, this);
		
	}
	
	
	private void handleNMS(int version) {
		NMSAsm.registerNMSClass(World.class);
		NMSAsm.registerNMSClass(EntityLiving.class);
		NMSAsm.registerNMSClass(CraftWorld.class);
		NMSAsm.registerNMSClass(Entity.class);
		NMSAsm.registerNMSClass(PlayerConnection.class);
		NMSAsm.registerNMSClass(EntityPlayer.class);	
		NMSAsm.registerNMSClass(CraftPlayer.class);	
		if (version >= 20) {
			NMSAsm.registerNMSClass(MinecraftKey.class);
			NMSAsm.registerNMSClass(EntityTypesv1_20.class);
			NMSAsm.registerNMSClass(EntityEnderDragonv1_20.class);
			NMSAsm.registerNMSClass(PacketPlayOutSpawnEntityv1_20.class);
		} else if (version >= 17) {
			NMSAsm.registerNMSClass(MinecraftKey.class);
			NMSAsm.registerNMSClass(EntityTypesv1_17.class);
			NMSAsm.registerNMSClass(EntityEnderDragonv1_17.class);
			NMSAsm.registerNMSClass(PacketPlayOutSpawnEntityLiving.class);
		} else {
			NMSAsm.registerNMSClass(EntityEnderDragonv1_8.class);
			NMSAsm.registerNMSClass(PacketPlayOutSpawnEntityLiving.class);
		}
	}
	
	public void spawnEnderDragon(int version, Location location, Player player) {
		EntityLiving entityLiving = null;
		CraftWorld craftWorld = NMSAsm.get(CraftWorld.class, location.getWorld());
		Object packetToSend = null;
		if (version >= 20) {
			EntityEnderDragonv1_20 enderDragon = NMSAsm.get(EntityEnderDragonv1_20.class);
			enderDragon.create(NMSAsm.get(EntityTypesv1_20.class).enderDragonType(), craftWorld.getHandle());
			enderDragon.pos(location.getX(), location.getY(), location.getZ());
			entityLiving = NMSAsm.get(EntityLiving.class, enderDragon.getHandle());
			PacketPlayOutSpawnEntityv1_20 spawnPacket = NMSAsm.get(PacketPlayOutSpawnEntityv1_20.class);
			spawnPacket.create(NMSAsm.get(Entity.class, enderDragon.getHandle()));
			packetToSend = spawnPacket.getHandle();
		} else if (version >= 17) {
			EntityEnderDragonv1_17 enderDragon = NMSAsm.get(EntityEnderDragonv1_17.class);
			enderDragon.create(NMSAsm.get(EntityTypesv1_17.class).enderDragonType(), craftWorld.getHandle());
			enderDragon.pos(location.getX(), location.getY(), location.getZ());
			entityLiving = NMSAsm.get(EntityLiving.class, enderDragon.getHandle());
			PacketPlayOutSpawnEntityLiving spawnPacket = NMSAsm.get(PacketPlayOutSpawnEntityLiving.class);
			spawnPacket.create(entityLiving);
			packetToSend = spawnPacket.getHandle();
		} else {
			EntityEnderDragonv1_8 enderDragon = NMSAsm.get(EntityEnderDragonv1_8.class);
			enderDragon.create(craftWorld.getHandle());
			enderDragon.pos(location.getX(), location.getY(), location.getZ());
			entityLiving = NMSAsm.get(EntityLiving.class, enderDragon.getHandle());
			PacketPlayOutSpawnEntityLiving spawnPacket = NMSAsm.get(PacketPlayOutSpawnEntityLiving.class);
			spawnPacket.create(entityLiving);
			packetToSend = spawnPacket.getHandle();
		}
		
		CraftPlayer craftPlayer = NMSAsm.get(CraftPlayer.class, player);
		EntityPlayer entityPlayer = craftPlayer.getHandle();
		
		PlayerConnection playerConnection = entityPlayer.playerConnection();
		playerConnection.sendPacket(packetToSend);
		
	}
	
	
	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		
		spawnEnderDragon(version, player.getLocation(), player);

//		CraftPlayer craftPlayer = NMSAsm.get(CraftPlayer.class, player);
//		craftPlayer.sendMessage("test test");
//		EntityPlayer entityPlayer = NMSAsm.get(EntityPlayer.class, craftPlayer.getHandle());
//		PlayerConnection playerConnection = NMSAsm.get(PlayerConnection.class, entityPlayer.playerConnection());
//		craftPlayer.sendMessage(entityPlayer.playerConnection().toString());
//		ChatComponentText comp = NMSAsm.get(ChatComponentText.class);
//		comp.construct("Testing inventory");
//		PacketPlayOutOpenWindow window = NMSAsm.get(PacketPlayOutOpenWindow.class);
//		window.constructor(entityPlayer.containerCounter(), "minecraft:chest", comp.getHandle(), 27);
//		PacketPlayOutChat chat = NMSAsm.get(PacketPlayOutChat.class);
//		chat.constructor(null, (byte) 1);
//		chat.components(TextComponent.fromLegacyText("Testing chat"));
//		playerConnection.sendPacket(chat.getHandle());
//		
//		Bukkit.getScheduler().runTaskLater(this, () -> playerConnection.sendPacket(window.getHandle()), 20);
//		Bukkit.getScheduler().runTaskLater(this, () -> playerConnection.sendPacket(chat.getHandle()), 20);
//		
//		CraftWorld world = NMSAsm.get(CraftWorld.class, player.getWorld());
//		EntityCreeper creeper = NMSAsm.get(EntityCreeper.class);
//		creeper.constructor(world.getHandle());
//		
//		Location loc = player.getLocation().clone();
//		
//		creeper.pos(loc.getX(), loc.getY(), loc.getZ());
//		creeper.yaw((loc.getYaw()) % 360.0f);
//		creeper.headRotation((loc.getYaw()) % 360.0f);
//		creeper.pitch((loc.getPitch()) % 360.0f);
//		PacketPlayOutSpawnEntityLiving spawnPacket = NMSAsm.get(PacketPlayOutSpawnEntityLiving.class);
//		spawnPacket.create(creeper.getHandle());
//		playerConnection.sendPacket(spawnPacket.getHandle());
//	
//		Vector dir = loc.getDirection();
//		
//		Bukkit.getScheduler().runTaskTimer(this, () -> {
//			PacketPlayOutEntityRelMove entityTeleport = NMSAsm.get(PacketPlayOutEntityRelMove.class);
//			entityTeleport.create(creeper.entityId(), (byte) (dir.getX() * 8), (byte) (dir.getY() * 8), (byte) (dir.getZ() * 8), true);
//			playerConnection.sendPacket(entityTeleport.getHandle());
//			
//		}, 0, 1);
		
		
	}
	

}
