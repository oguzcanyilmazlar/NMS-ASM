package me.acablade.nmsasm;

import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.FieldInfoList;
import io.github.classgraph.MethodInfoList;
import io.github.classgraph.ScanResult;

public class NMSAsm {
	
	private static Map<Class<?>, Class<?>> registered = new HashMap<>();
	private static Map<Class<?>, Class<?>> normalToNMS = new HashMap<>();
	private static Map<String, Class<?>> simpleToNMSClass = new HashMap<>();
	
	private static ScanResult scanResult = null;
	
	public static Class<?> registerNMSClass(Class<?> clazz) {
		if(scanResult == null) {
			scanResult = new ClassGraph().enableAllInfo().acceptPackages("net.minecraft", "org.bukkit.craftbukkit").scan();
		}
		if(registered.containsKey(clazz)) throw new NMSException(String.format("%s is already registered", clazz.getCanonicalName()));
		if(!clazz.isInterface()) throw new NMSException(String.format("Class(%s) should be specified as an interface", clazz.getCanonicalName()));
		if(!clazz.isAnnotationPresent(NMS.class)) throw new NMSException(String.format("Class(%s) does not have NMS annotation", clazz.getCanonicalName()));

		String clazzName = clazz.getAnnotation(NMS.class).value();
		
		if(clazzName.isEmpty()) {
			clazzName = clazz.getSimpleName();
			
		}
		
		Class<?> wantedClass = getClass(clazzName);
		System.out.println(wantedClass.getCanonicalName());
		
		ClassWriter writer = GeneratorAdapter.newClassWriter(clazz.getName() + "$NMSImpl", Type.getInternalName(clazz));
		GeneratorAdapter.addField(Opcodes.ACC_PUBLIC, "handle", "Ljava/lang/Object;", null, null, writer);

		
		GeneratorAdapter.writeConstructor(clazz, writer);
		GeneratorAdapter.writeEmptyConstructor(clazz, writer);
		for(Method method: clazz.getMethods()) {
			if(method.isAnnotationPresent(NMSConstructor.class)) {
				try {
					
					Constructor<?> constructor = wantedClass.getConstructor(getParameters(clazz, method).toArray(new Class<?>[0]));
					GeneratorAdapter adapter = GeneratorAdapter.newMethodGenerator(writer, method.getName(), getMethodDescriptor(method));
			        adapter.loadThis();
			        adapter.visitTypeInsn(Opcodes.NEW, Type.getInternalName(wantedClass));
			        adapter.dup();
			        int index = adapter.getArgIndex(0);
			        for (int i = 0; i < adapter.argumentTypes.length; ++i) {
			            Type argumentType = adapter.argumentTypes[i];
			            Parameter param = constructor.getParameters()[i];
			            adapter.loadInsn(argumentType, index);
			            if(!Type.getType(param.getType()).equals(argumentType)) {
			            	adapter.checkCast(Type.getType(param.getType()));
			            }
			            index += argumentType.getSize();
			        }
			        adapter.visitMethodInsn(Opcodes.INVOKESPECIAL, Type.getInternalName(wantedClass), "<init>", Type.getConstructorDescriptor(constructor), false);
			        adapter.putField(Type.getObjectType(Type.getInternalName(clazz) + "$NMSImpl"), "handle", GeneratorAdapter.OBJECT_TYPE);
			        adapter.returnValue();
			        adapter.endMethod();
			        
			        
				} catch (NoSuchMethodException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (SecurityException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				continue;
			}
			
			if(!method.isAnnotationPresent(NMS.class)) {
				
				if(method.getName().equals("getHandle") && method.getReturnType().equals(Object.class)) {
					GeneratorAdapter adapter = GeneratorAdapter.newMethodGenerator(writer, "getHandle", Type.getMethodDescriptor(method));
					adapter.loadThis();
					adapter.getField(Type.getObjectType(Type.getInternalName(clazz) + "$NMSImpl"), "handle", Type.getObjectType("java/lang/Object"));
					adapter.returnValue();
					adapter.endMethod();
				}
				
				continue;
			}
			
			NMS nmsAnnotation = method.getAnnotation(NMS.class);
			
			CallType callType = nmsAnnotation.callType();
		
					
			if(callType == CallType.METHOD) {
				GeneratorAdapter adapter = GeneratorAdapter.newMethodGenerator(writer, method.getName(), Type.getMethodDescriptor(method));
				String methodName = nmsAnnotation.value();
				
				if(methodName.isEmpty()) {
					
				}
				
				List<Class<?>> paramList = getParameters(clazz, method);
				try {
					// ugly asf
					String interfaceClass = nmsAnnotation.interfaceName().isEmpty() ?
							(!nmsAnnotation.interfaceClass().equals(Object.class) ? normalToNMS.get(nmsAnnotation.interfaceClass()).getCanonicalName() : "") :
								nmsAnnotation.interfaceName();
					
					Method declared = null;
					if(methodName.isEmpty()) {
						// FIND BY DESCRIPTOR
						String descriptor = getMethodDescriptor(method);
						declared = getMethodByDescriptor(interfaceClass.isEmpty() ? wantedClass : getClass(interfaceClass), descriptor);
					}else {
						// FIND BY NAME
						declared = (interfaceClass.isEmpty() ? wantedClass : getClass(interfaceClass)).getDeclaredMethod(methodName, paramList.toArray(new Class<?>[0]));
					}
					
					adapter.loadThis();
					adapter.getField(Type.getObjectType(Type.getInternalName(clazz) + "$NMSImpl"), "handle", Type.getObjectType("java/lang/Object"));
					adapter.checkCast(Type.getType(wantedClass));
					adapter.loadArgs();
					adapter.invokeVirtual(Type.getType(wantedClass), me.acablade.nmsasm.Method.getMethod(declared));
				} catch (NoSuchMethodException e) {
					// TODO Auto-generated catch block
					throw new NMSException(String.format("(%s) Cant find the wanted method: %s", method.getName(), methodName));
				} catch (SecurityException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				adapter.returnValue();
				adapter.visitMaxs(20, 20);
				adapter.endMethod();
			} else if(callType == CallType.STATIC_METHOD) {
				GeneratorAdapter adapter = GeneratorAdapter.newMethodGenerator(writer, method.getName(), Type.getMethodDescriptor(method));
				String methodName = nmsAnnotation.value();
				List<Class<?>> paramList = getParameters(clazz, method);
				try {
					Method declared = null;
					
					if(methodName.isEmpty()) {
						// FIND BY DESCRIPTOR
						String descriptor = getMethodDescriptor(method);
						declared = getMethodByDescriptor(wantedClass, descriptor);
					} else {
						// FIND BY NAME
						declared = wantedClass.getDeclaredMethod(methodName, paramList.toArray(new Class<?>[0]));
					}
					adapter.loadArg(0);
					adapter.invokeStatic(Type.getType(wantedClass), me.acablade.nmsasm.Method.getMethod(declared));
				} catch (NoSuchMethodException e) {
					// TODO Auto-generated catch block
					throw new NMSException(String.format("(%s) Cant find the wanted method: %s", method.getName(), methodName));
				} catch (SecurityException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				adapter.returnValue();
				adapter.visitMaxs(20, 20);
				adapter.endMethod();
				
				
			} else if(callType == CallType.FIELD) {
				GeneratorAdapter adapter = GeneratorAdapter.newMethodGenerator(writer, method.getName(), Type.getMethodDescriptor(method));
				String fieldName = method.getAnnotation(NMS.class).value();
				if(method.getReturnType() == Void.TYPE) {
					// SETTER
					try {
						Class<?> field = fieldName.isEmpty() ?
								getFieldByType(wantedClass, Type.getDescriptor(method.getParameters()[0].getType())).getType() :
								wantedClass.getField(fieldName).getType();
						
						adapter.loadThis();
						adapter.getField(Type.getObjectType(Type.getInternalName(clazz) + "$NMSImpl"), "handle", Type.getObjectType("java/lang/Object"));
						adapter.checkCast(Type.getType(wantedClass));
						adapter.loadArg(0);
						adapter.visitFieldInsn(Opcodes.PUTFIELD, Type.getInternalName(wantedClass), fieldName, Type.getDescriptor(field));
					} catch (NoSuchFieldException | SecurityException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					
					
				}else {
					// GETTER
					try {
						Field field = fieldName.isEmpty() ? 
								getFieldByType(wantedClass, Type.getDescriptor(normalToNMS.containsKey(method.getReturnType()) ? normalToNMS.get(method.getReturnType()) : method.getReturnType())) :
								wantedClass.getField(fieldName);
						
						adapter.loadThis();
						adapter.getField(Type.getObjectType(Type.getInternalName(clazz) + "$NMSImpl"), "handle", Type.getObjectType("java/lang/Object"));
						adapter.checkCast(Type.getType(wantedClass));
						adapter.getField(Type.getType(wantedClass), field.getName(), Type.getType(field.getType()));
					} catch (NoSuchFieldException | SecurityException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
				adapter.returnValue();
				adapter.visitMaxs(20, 20);
				adapter.endMethod();
				
				
				
			}
			
		}
		
		writer.visitEnd();
		
		Class clz = GeneratedClassDefiner.INSTANCE
				.define(clazz.getClassLoader(), clazz.getName() + "$NMSImpl", writer.toByteArray());
		registered.put(clazz, clz);
		normalToNMS.put(clazz, wantedClass);
		
		return clz;
		
		
	}
	
	private static Method getMethodByDescriptor(Class<?> clazz, String descriptor) {
		MethodInfoList list = scanResult.getClassInfo(clazz.getCanonicalName()).getMethodInfo().filter((methodInfo) -> {
			return methodInfo.getTypeDescriptorStr().equals(descriptor);
		});
		if(list.size() == 0) throw new NMSException(String.format("No method found with the descriptor %s in class \"%s\"", descriptor, clazz.getCanonicalName()));
		if(list.size() != 1) throw new NMSException(String.format("Found more than one method with the descriptor %s in class \"%s\"", descriptor, clazz.getCanonicalName()));
		return list.get(0).loadClassAndGetMethod();
	}
	
	private static Field getFieldByType(Class<?> clazz, String descriptor) throws NoSuchFieldException, SecurityException {
		FieldInfoList list = scanResult.getClassInfo(clazz.getCanonicalName()).getFieldInfo().filter((fieldInfo) -> {
			return fieldInfo.getTypeDescriptorStr().equals(descriptor);
		});
		if(list.size() == 0) throw new NMSException(String.format("No field found with the descriptor %s in class \"%s\"", descriptor, clazz.getCanonicalName()));
		if(list.size() != 1) throw new NMSException(String.format("Found more than one field with the descriptor %s in class \"%s\"", descriptor, clazz.getCanonicalName()));
		return list.get(0).loadClassAndGetField();
	}
	
	private static List<String> getClassesFromClassGraph(String name){
		name = name.contains(".") ? name : "." + name;
		String finalName = name;
		
		SearchType type = SearchType.ALL;
		
		if(name.contains("nms.")) type = SearchType.NMS;
		else if(name.contains("craftbukkit.")) type = SearchType.BUKKIT;
		
		return scanResult.getAllClassesAsMap().keySet().stream().filter(searchAlgo(type, finalName)).collect(Collectors.toList());
	}
	
	private static Predicate<String> searchAlgo(SearchType searchType, String name){
		return t -> {
			
			String simpleName = name.substring(name.lastIndexOf('.'), name.length());
			switch (searchType) {
				case NMS:
					return t.contains("net.minecraft") && t.endsWith(simpleName);
				case BUKKIT:
					return t.contains("org.bukkit") && t.endsWith(simpleName);
				case ALL:
					return t.endsWith(name);
			}
			return false;
		};
	}
	
	private static enum SearchType {
		NMS, BUKKIT, ALL;
	}
	
	private static Class<?> getClass(String name){
		return simpleToNMSClass.computeIfAbsent(name, (t) -> {
			List<String> classes = getClassesFromClassGraph(t);
			if(classes.size() == 0) throw new NMSException(String.format("%s found zero classes", t));
			if(classes.size() != 1) throw new NMSException(String.format("%s found two or more of classes that have the same name, please use full name instead (%s)", name, String.join(", ", classes)));
			try {
				return Class.forName(classes.get(0));
			} catch (ClassNotFoundException e) {
				// unreachable
				throw new NMSException(String.format("%s class not found", classes.get(0)));
			}
		});
	}
	
	public static <T> T get(Class<T> clazz, Object... objects) {
		
		Class<T> clz = (Class<T>) registered.get(clazz);
		
		for(Constructor constructor : clz.getConstructors()) {
			if(constructor.getParameterCount() == objects.length)
				try {
					return (T) constructor.newInstance(objects);
				} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
						| InvocationTargetException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		
		throw new NMSException(String.format("Constructor not found (%s)", clazz.getCanonicalName()));
	}
	
	
	private static List<Class<?>> getParameters(Class clazz, Method method){
		List<Class<?>> paramList = new ArrayList<>();
		for(Parameter param: method.getParameters()) {
			if(!param.isAnnotationPresent(NMS.class)) {
				if(normalToNMS.containsKey(param.getType())) {
					paramList.add(normalToNMS.get(param.getType()));
				}else {
					paramList.add(param.getType());
				}
				
				continue;
			}
			String paramClass = param.getAnnotation(NMS.class).value();
			paramList.add(getClass(paramClass));
			
		}
		return paramList;
	}
	
	public static String getMethodDescriptor(final Method method) {
	    StringBuilder stringBuilder = new StringBuilder();
	    stringBuilder.append('(');
	    Parameter[] parameters = method.getParameters();
	    for (Parameter parameter : parameters) {
	    	if(!parameter.isAnnotationPresent(NMS.class)) {
	    		if(normalToNMS.containsKey(parameter.getType())) {
	    			appendDescriptor(normalToNMS.get(parameter.getType()), stringBuilder);
				}else {
					appendDescriptor(parameter.getType(), stringBuilder);
				}
			}else {
				NMS nms = parameter.getAnnotation(NMS.class);
				stringBuilder.append('L').append(Type.getInternalName(getClass(nms.value()))).append(';');
			}
	      
	    }
	    stringBuilder.append(')');
	    AnnotatedType type = method.getAnnotatedReturnType();
	    if(!method.getAnnotatedReturnType().isAnnotationPresent(NMS.class)) {
	    	
	    	if(normalToNMS.containsKey(method.getReturnType())) {
    			appendDescriptor(normalToNMS.get(method.getReturnType()), stringBuilder);
			}else {
				appendDescriptor(method.getReturnType(), stringBuilder);
			}
	    	
	    } else {
	    	NMS nms = type.getAnnotation(NMS.class);
	    	stringBuilder.append('L').append(Type.getInternalName(getClass(nms.value()))).append(';');
	    }
	    return stringBuilder.toString();
	  }
	
	
	private static void appendDescriptor(final Class<?> clazz, final StringBuilder stringBuilder) {
	    Class<?> currentClass = clazz;
	    while (currentClass.isArray()) {
	      stringBuilder.append('[');
	      currentClass = currentClass.getComponentType();
	    }
	    if (currentClass.isPrimitive()) {
	      char descriptor;
	      if (currentClass == Integer.TYPE) {
	        descriptor = 'I';
	      } else if (currentClass == Void.TYPE) {
	        descriptor = 'V';
	      } else if (currentClass == Boolean.TYPE) {
	        descriptor = 'Z';
	      } else if (currentClass == Byte.TYPE) {
	        descriptor = 'B';
	      } else if (currentClass == Character.TYPE) {
	        descriptor = 'C';
	      } else if (currentClass == Short.TYPE) {
	        descriptor = 'S';
	      } else if (currentClass == Double.TYPE) {
	        descriptor = 'D';
	      } else if (currentClass == Float.TYPE) {
	        descriptor = 'F';
	      } else if (currentClass == Long.TYPE) {
	        descriptor = 'J';
	      } else {
	        throw new AssertionError();
	      }
	      stringBuilder.append(descriptor);
	    } else {
	      stringBuilder.append('L').append(Type.getInternalName(currentClass)).append(';');
	    }
	  }

}
