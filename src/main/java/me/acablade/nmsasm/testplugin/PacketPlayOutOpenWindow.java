package me.acablade.nmsasm.testplugin;

import me.acablade.nmsasm.NMS;
import me.acablade.nmsasm.NMSConstructor;

@NMS
public interface PacketPlayOutOpenWindow {
	
	
	@NMSConstructor
	public void constructor(int i, String s, @NMS("IChatBaseComponent") Object baseComponent, int k);
	
	public Object getHandle();
	
}
