package me.acablade.nmsasm.testplugin;

import me.acablade.nmsasm.NMS;

@NMS("nms.PlayerConnection")
public interface PlayerConnection {
	
	@NMS
	public void sendPacket(@NMS("nms.Packet") Object packet);
	
	public Object getHandle();
	
}
