package me.acablade.nmsasm.testplugin.nms.v1_17;

import me.acablade.nmsasm.CallType;
import me.acablade.nmsasm.NMS;
import me.acablade.nmsasm.NMSConstructor;
import me.acablade.nmsasm.testplugin.nms.Entity;
import me.acablade.nmsasm.testplugin.nms.EntityLiving;
import me.acablade.nmsasm.testplugin.nms.World;

@NMS("EntityEnderDragon")
public interface EntityEnderDragonv1_17 {
	
	@NMSConstructor
	public void create(@NMS("nms.EntityTypes") Object entityTypes, @NMS("nms.World") Object world);
	
	
	@NMS(value = "setPosition", interfaceClass = Entity.class)
	public void pos(double x, double y, double z);
	
	@NMS(value = "getId", interfaceClass = Entity.class)
	public int entityId();
	
	@NMS(value = "aZ", interfaceClass = EntityLiving.class, callType = CallType.FIELD)
	public void headRotation(float yaw);
	
	@NMS(value = "ay", interfaceClass = Entity.class, callType = CallType.FIELD)
	public void yaw(float yaw);
	
	@NMS(value = "az", interfaceClass = Entity.class, callType = CallType.FIELD)
	public void pitch(float pitch);
	
	public Object getHandle();	
}
