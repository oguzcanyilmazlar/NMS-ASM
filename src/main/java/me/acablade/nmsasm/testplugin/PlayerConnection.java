package me.acablade.nmsasm.testplugin;

import me.acablade.nmsasm.NMS;

@NMS
public interface PlayerConnection {
	
	@NMS("sendPacket")
	public void sendPacket(@NMS("net.minecraft.server.v1_8_R3.Packet") Object packet);
	
	public Object getHandle();
	
}
