package me.acablade.nmsasm.testplugin.nms;

import me.acablade.nmsasm.NMS;

@NMS
public interface PlayerConnection {
	
	@NMS
	public void sendPacket(@NMS("nms.Packet") Object packet);
	
}
