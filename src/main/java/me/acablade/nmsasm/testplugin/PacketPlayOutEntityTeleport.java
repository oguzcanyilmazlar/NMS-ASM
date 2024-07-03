package me.acablade.nmsasm.testplugin;

import me.acablade.nmsasm.NMS;
import me.acablade.nmsasm.NMSConstructor;

@NMS("net.minecraft.server.v1_8_R3.PacketPlayOutEntityTeleport")
public interface PacketPlayOutEntityTeleport {
	
	@NMSConstructor
	public void create(int entityId, int x, int y, int z, byte yaw, byte pitch, boolean onGround);
	
	public Object getHandle();
	
}
