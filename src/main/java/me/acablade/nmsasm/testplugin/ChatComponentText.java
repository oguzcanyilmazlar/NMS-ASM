package me.acablade.nmsasm.testplugin;

import me.acablade.nmsasm.NMS;
import me.acablade.nmsasm.NMSConstructor;

@NMS("ChatComponentText")
public interface ChatComponentText {
	
	@NMSConstructor
	public void construct(String b);
	
	@NMS("getText")
	public String getText();
	
	public Object getHandle();
	
}
