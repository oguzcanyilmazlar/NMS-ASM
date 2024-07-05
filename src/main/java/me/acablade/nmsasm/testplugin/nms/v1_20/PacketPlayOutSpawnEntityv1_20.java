package me.acablade.nmsasm.testplugin.nms.v1_20;

import me.acablade.nmsasm.NMS;
import me.acablade.nmsasm.NMSConstructor;

@NMS("PacketPlayOutSpawnEntity")
public interface PacketPlayOutSpawnEntityv1_20 {
	
	@NMSConstructor
	public void create(@NMS("nms.Entity") Object entity);
	
	public Object getHandle();
	
}
