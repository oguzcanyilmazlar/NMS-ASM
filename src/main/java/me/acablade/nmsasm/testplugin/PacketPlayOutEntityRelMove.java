package me.acablade.nmsasm.testplugin;

import me.acablade.nmsasm.NMS;
import me.acablade.nmsasm.NMSConstructor;

@NMS("net.minecraft.server.v1_8_R3.PacketPlayOutEntity$PacketPlayOutRelEntityMove")
public interface PacketPlayOutEntityRelMove {
	
	@NMSConstructor
	public void create(int entityId, byte x, byte y, byte z, boolean onGround);
	
	public Object getHandle();
	
}
