package me.acablade.nmsasm.testplugin.nms.v1_20;

import me.acablade.nmsasm.CallType;
import me.acablade.nmsasm.NMS;
import me.acablade.nmsasm.NMSConstructor;
import me.acablade.nmsasm.testplugin.nms.Entity;
import me.acablade.nmsasm.testplugin.nms.EntityLiving;
import me.acablade.nmsasm.testplugin.nms.World;

@NMS("EntityEnderDragon")
public interface EntityEnderDragonv1_20 {
	
	@NMSConstructor
	public void create(EntityTypesv1_20<? extends EntityEnderDragonv1_20> entityTypes, World world);
	
	
	@NMS(value = "e", interfaceClass = Entity.class)
	public void pos(double x, double y, double z);
	
	@NMS(value = "aj", interfaceClass = Entity.class)
	public int entityId();
	
	@NMS(value = "aW", interfaceClass = EntityLiving.class, callType = CallType.FIELD)
	public void headRotation(float yaw);
	
	@NMS(value = "r", interfaceClass = Entity.class)
	public void yaw(float yaw);
	
	@NMS(value = "s", interfaceClass = Entity.class)
	public void pitch(float pitch);
	
	public Object getHandle();	
}
