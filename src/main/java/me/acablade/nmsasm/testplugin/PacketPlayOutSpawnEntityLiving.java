package me.acablade.nmsasm.testplugin;

import me.acablade.nmsasm.NMS;
import me.acablade.nmsasm.NMSConstructor;

@NMS("net.minecraft.server.v1_8_R3.PacketPlayOutSpawnEntityLiving")
public interface PacketPlayOutSpawnEntityLiving {
	
	@NMSConstructor
	public void create(@NMS("net.minecraft.server.v1_8_R3.EntityLiving") Object entity);
	
	public Object getHandle();
	
}
