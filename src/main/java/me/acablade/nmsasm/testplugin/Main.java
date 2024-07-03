package me.acablade.nmsasm.testplugin;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import me.acablade.nmsasm.NMSAsm;
import net.md_5.bungee.api.chat.TextComponent;

public class Main extends JavaPlugin implements Listener{
	
	@Override
	public void onEnable() {
		
		NMSAsm.registerNMSClass(CraftPlayer.class);
		NMSAsm.registerNMSClass(EntityPlayer.class);
		NMSAsm.registerNMSClass(PlayerConnection.class);
		NMSAsm.registerNMSClass(PacketPlayOutChat.class);
		NMSAsm.registerNMSClass(IChatBaseComponent.class);
		NMSAsm.registerNMSClass(ChatComponentText.class);
		NMSAsm.registerNMSClass(PacketPlayOutOpenWindow.class);
		NMSAsm.registerNMSClass(CraftLivingEntity.class);
		NMSAsm.registerNMSClass(CraftWorld.class);
		NMSAsm.registerNMSClass(EntityCreeper.class);
		NMSAsm.registerNMSClass(PacketPlayOutSpawnEntityLiving.class);
		NMSAsm.registerNMSClass(PacketPlayOutEntityTeleport.class);
		NMSAsm.registerNMSClass(PacketPlayOutEntityRelMove.class);
		
		Bukkit.getPluginManager().registerEvents(this, this);
		
		
	}
	
	
	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();

		CraftPlayer craftPlayer = NMSAsm.get(CraftPlayer.class, player);
		craftPlayer.sendMessage("test test");
		EntityPlayer entityPlayer = NMSAsm.get(EntityPlayer.class, craftPlayer.getHandle());
		PlayerConnection playerConnection = NMSAsm.get(PlayerConnection.class, entityPlayer.playerConnection());
		craftPlayer.sendMessage(entityPlayer.playerConnection().toString());
		ChatComponentText comp = NMSAsm.get(ChatComponentText.class);
		comp.construct("Testing inventory");
		PacketPlayOutOpenWindow window = NMSAsm.get(PacketPlayOutOpenWindow.class);
		window.constructor(entityPlayer.containerCounter(), "minecraft:chest", comp.getHandle(), 27);
		PacketPlayOutChat chat = NMSAsm.get(PacketPlayOutChat.class);
		chat.constructor(null, (byte) 1);
		chat.components(TextComponent.fromLegacyText("Testing chat"));
		playerConnection.sendPacket(chat.getHandle());
		
		Bukkit.getScheduler().runTaskLater(this, () -> playerConnection.sendPacket(window.getHandle()), 20);
		Bukkit.getScheduler().runTaskLater(this, () -> playerConnection.sendPacket(chat.getHandle()), 20);
		
		CraftWorld world = NMSAsm.get(CraftWorld.class, player.getWorld());
		EntityCreeper creeper = NMSAsm.get(EntityCreeper.class);
		creeper.constructor(world.getHandle());
		
		Location loc = player.getLocation().clone();
		
		creeper.pos(loc.getX(), loc.getY(), loc.getZ());
		creeper.yaw((loc.getYaw()) % 360.0f);
		creeper.headRotation((loc.getYaw()) % 360.0f);
		creeper.pitch((loc.getPitch()) % 360.0f);
		PacketPlayOutSpawnEntityLiving spawnPacket = NMSAsm.get(PacketPlayOutSpawnEntityLiving.class);
		spawnPacket.create(creeper.getHandle());
		playerConnection.sendPacket(spawnPacket.getHandle());
	
		Vector dir = loc.getDirection();
		
		Bukkit.getScheduler().runTaskTimer(this, () -> {
			PacketPlayOutEntityRelMove entityTeleport = NMSAsm.get(PacketPlayOutEntityRelMove.class);
			entityTeleport.create(creeper.entityId(), (byte) (dir.getX() * 8), (byte) (dir.getY() * 8), (byte) (dir.getZ() * 8), true);
			playerConnection.sendPacket(entityTeleport.getHandle());
			
		}, 0, 1);
		
		
	}
	

}
