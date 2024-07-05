package me.acablade.nmsasm.testplugin.nms.v1_20;

import me.acablade.nmsasm.CallType;
import me.acablade.nmsasm.NMS;
import me.acablade.nmsasm.testplugin.nms.v1_17.MinecraftKey;

@NMS("EntityTypes")
public interface EntityTypesv1_20<T> {

	@NMS(value = "D", callType = CallType.STATIC_FIELD)
	public EntityTypesv1_20<EntityEnderDragonv1_20> enderDragonType();
	
	
	@NMS(callType = CallType.STATIC_METHOD)
	public MinecraftKey getName(EntityTypesv1_20<?> types);
	
}
