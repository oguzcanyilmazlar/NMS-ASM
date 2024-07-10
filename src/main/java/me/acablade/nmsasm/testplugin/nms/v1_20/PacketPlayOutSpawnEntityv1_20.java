package me.acablade.nmsasm.testplugin.nms.v1_20;

import me.acablade.nmsasm.NMS;
import me.acablade.nmsasm.NMSConstructor;
import me.acablade.nmsasm.testplugin.nms.Entity;

@NMS("PacketPlayOutSpawnEntity")
public interface PacketPlayOutSpawnEntityv1_20 {
	
	@NMSConstructor
	public void create(Entity entity);
	
	public Object getHandle();
	
}
