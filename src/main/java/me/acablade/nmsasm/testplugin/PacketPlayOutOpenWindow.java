package me.acablade.nmsasm.testplugin;

import me.acablade.nmsasm.NMS;
import me.acablade.nmsasm.NMSConstructor;

@NMS("net.minecraft.server.v1_8_R3.PacketPlayOutOpenWindow")
public interface PacketPlayOutOpenWindow {
	
	
	@NMSConstructor
	public void constructor(int i, String s, @NMS("net.minecraft.server.v1_8_R3.IChatBaseComponent") Object baseComponent, int k);
	
	public Object getHandle();
	
}
