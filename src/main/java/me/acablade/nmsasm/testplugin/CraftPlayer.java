package me.acablade.nmsasm.testplugin;

import me.acablade.nmsasm.NMS;

@NMS("CraftPlayer")
public interface CraftPlayer {
	
	@NMS("getHandle")
	public Object getHandle();
	
	@NMS("sendMessage")
	public void sendMessage(String message);

}
