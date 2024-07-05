package me.acablade.nmsasm.testplugin.nms.v1_17;

import me.acablade.nmsasm.CallType;
import me.acablade.nmsasm.NMS;

@NMS("EntityTypes")
public interface EntityTypesv1_17<T> {

	@NMS(value = "v", callType = CallType.STATIC_FIELD)
	public EntityTypesv1_17<EntityEnderDragonv1_17> enderDragonType();
	
	
	@NMS(callType = CallType.STATIC_METHOD)
	public MinecraftKey getName(EntityTypesv1_17<?> types);
	
}
