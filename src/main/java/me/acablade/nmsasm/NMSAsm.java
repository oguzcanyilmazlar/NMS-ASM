package me.acablade.nmsasm;

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
//	  adapter.visitTypeInsn(Opcodes.NEW, Type.getInternalName(wantedClass));
//    adapter.dup();
//    loadArgs(adapter, constructor);
//    adapter.visitMethodInsn(Opcodes.INVOKESPECIAL, Type.getInternalName(wantedClass), "<init>", Type.getConstructorDescriptor(constructor), false);
	
	
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
		normalToNMS.put(clazz, wantedClass);
		
		ClassWriter writer = GeneratorAdapter.newClassWriter(clazz.getName() + "$NMSImpl", Type.getInternalName(clazz));
		
		// add public Object handle;
		GeneratorAdapter.addField(Opcodes.ACC_PUBLIC, "handle", "Ljava/lang/Object;", null, null, writer);

		GeneratorAdapter.writeConstructor(clazz, writer);
		GeneratorAdapter.writeEmptyConstructor(clazz, writer);
		for(Method method: clazz.getMethods()) {
			if(method.isAnnotationPresent(NMSConstructor.class)) {
				createConstructorMethod(writer, method, wantedClass, clazz);
				continue;
			}
			
			if(!method.isAnnotationPresent(NMS.class)) {
				
				if(method.getName().equals("getHandle") && method.getReturnType().equals(Object.class)) {
					createGetHandleMethod(writer, clazz, method);
				}
				
				continue;
			}
			
			NMS nmsAnnotation = method.getAnnotation(NMS.class);
			
			String nmsMethodName = nmsAnnotation.value();
			
			CallType callType = nmsAnnotation.callType();
		
					
			if(callType == CallType.METHOD) {
				GeneratorAdapter adapter = GeneratorAdapter.newMethodGenerator(writer, method.getName(), Type.getMethodDescriptor(method));
				
				List<Class<?>> paramList = getParameters(clazz, method);
				Class<?> interfaceClass = getMethodInterface(nmsAnnotation);
				Class<?> finalClass = interfaceClass != null ? interfaceClass : wantedClass;
				Method declared = getWantedMethod(method, nmsMethodName, finalClass, paramList.toArray(new Class<?>[0]));
				createMethod(adapter, clazz, finalClass, declared, method.getReturnType());
			} else if(callType == CallType.STATIC_METHOD) {
				GeneratorAdapter adapter = GeneratorAdapter.newMethodGenerator(writer, method.getName(), Type.getMethodDescriptor(method));
				List<Class<?>> paramList = getParameters(clazz, method);
				
				Type returnImplType = getImplType(method.getReturnType());
				
				
				Method declared = getWantedMethod(method, nmsMethodName, wantedClass, paramList.toArray(new Class<?>[0]));
				if(normalToNMS.containsKey(method.getReturnType())) {
					adapter.newInstance(returnImplType);
					adapter.dup();
				}
				loadArgs(adapter, declared);
				adapter.invokeStatic(Type.getType(wantedClass), me.acablade.nmsasm.Method.getMethod(declared));
				if(normalToNMS.containsKey(method.getReturnType())) {
					// new World$NMSImpl(method);
					adapter.invokeConstructor(returnImplType, new me.acablade.nmsasm.Method("<init>", "(Ljava/lang/Object;)V"));
				}
				adapter.returnValue();
				adapter.visitMaxs(20, 20);
				adapter.endMethod();
				
				
			} else if(callType == CallType.FIELD) {
				GeneratorAdapter adapter = GeneratorAdapter.newMethodGenerator(writer, method.getName(), Type.getMethodDescriptor(method));
				String fieldName = nmsMethodName;
				Field field = getWantedField(wantedClass, method, fieldName);
				
				Type implType = getImplType(clazz);
				
				if(method.getReturnType() == Void.TYPE) {
					// SETTER
					adapter.loadThis();
					adapter.getField(implType, "handle", GeneratorAdapter.OBJECT_TYPE);
					adapter.checkCast(Type.getType(wantedClass));
					adapter.loadArg(0);
					adapter.putField(Type.getType(wantedClass), fieldName, Type.getType(field.getType()));
				}else {
					// GETTER
					
					Type returnImplType = getImplType(method.getReturnType());
					
					if(normalToNMS.containsKey(method.getReturnType())) {
						adapter.newInstance(returnImplType);
						adapter.dup();
					}
					
					adapter.loadThis();
					
					
					adapter.getField(implType, "handle", GeneratorAdapter.OBJECT_TYPE);
					adapter.checkCast(Type.getType(wantedClass));
					adapter.getField(Type.getType(wantedClass), field.getName(), Type.getType(field.getType()));
					if(normalToNMS.containsKey(method.getReturnType())) {
						adapter.invokeConstructor(returnImplType, new me.acablade.nmsasm.Method("<init>", "(Ljava/lang/Object;)V"));
					}
				}
				
				adapter.returnValue();
				adapter.visitMaxs(20, 20);
				adapter.endMethod();
				
				
				
			} else if(callType == CallType.STATIC_FIELD) {
				GeneratorAdapter adapter = GeneratorAdapter.newMethodGenerator(writer, method.getName(), Type.getMethodDescriptor(method));
				String fieldName = nmsMethodName;
				
				Field field = getWantedField(wantedClass, method, fieldName);
				
				Type implType = getImplType(clazz);
				
				if(method.getReturnType() == Void.TYPE) {
					// SETTER
					adapter.loadThis();
					
					adapter.getField(implType, "handle", GeneratorAdapter.OBJECT_TYPE);
					adapter.checkCast(Type.getType(wantedClass));
					adapter.loadArg(0);
					adapter.putStatic(Type.getType(wantedClass), clazzName, Type.getType(field.getType()));
					
					
				}else {
					// GETTER
					
					
					Type returnImplType = getImplType(method.getReturnType());
					
					// weird issue with this where it doesnt matter if it implements the interface, can just return the class. (jvm fuckery)
					if(normalToNMS.containsKey(method.getReturnType())) {
						adapter.newInstance(returnImplType);
						adapter.dup();
					}
					adapter.getStatic(Type.getType(wantedClass), field.getName(), Type.getType(field.getType()));
					if(normalToNMS.containsKey(method.getReturnType())) {
						adapter.invokeConstructor(returnImplType, new me.acablade.nmsasm.Method("<init>", "(Ljava/lang/Object;)V"));
					}
				
					
					
				}
				
				adapter.returnValue();
				adapter.visitMaxs(20, 20);
				adapter.endMethod();
			}
			
		}
		
		writer.visitEnd();
		
		Class<?> clz = GeneratedClassDefiner
				.define(clazz.getClassLoader(), clazz.getName() + "$NMSImpl", writer.toByteArray());
		registered.put(clazz, clz);
		
		
		return clz;
		
		
	}
	
	private static Type getImplType(Class<?> clazz) {
		return Type.getObjectType(Type.getInternalName(clazz) + "$NMSImpl");
	}
	
	private static Field getWantedField(Class<?> wantedClass, Method currentMethod, String fieldName) {
		

		Field field = null;
		
		
		if(fieldName.isEmpty()) {
			Class<?> descriptor = currentMethod.getReturnType();
			if(normalToNMS.containsKey(currentMethod.getReturnType())) {
				descriptor = normalToNMS.get(currentMethod.getReturnType());
			}
			try {
				field = getFieldByType(wantedClass, Type.getDescriptor(descriptor));
			} catch (NoSuchFieldException | SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			try {
				field = wantedClass.getField(fieldName);
			} catch (NoSuchFieldException | SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
		return field;
	}
	
	private static Class<?> getMethodInterface(NMS nmsAnnotation) {
		String nmsAnnotationInterface = nmsAnnotation.interfaceName();
		Class<?> nmsAnnotationInterfaceClass = nmsAnnotation.interfaceClass();
		
		String interfaceClass = "";
		
		if(nmsAnnotationInterface.isEmpty()) {
			if(!nmsAnnotationInterfaceClass.equals(Object.class)) {
				interfaceClass = normalToNMS.get(nmsAnnotationInterfaceClass).getCanonicalName();
			}
		} else {
			interfaceClass = nmsAnnotationInterface;
		}
		
		if(interfaceClass.isEmpty()) return null;
		
		return getClass(interfaceClass);
	}
	
	private static Method getWantedMethod(Method currentMethod, String wantedMethodName, Class<?> wantedClass, Class<?>[] params) {
		Method declared = null;
		
		if(wantedMethodName.isEmpty()) {
			// FIND BY DESCRIPTOR
			String descriptor = getMethodDescriptor(currentMethod);
			declared = getMethodByDescriptor(wantedClass, descriptor);
		} else {
			// FIND BY NAME
			try {
				declared = wantedClass.getDeclaredMethod(wantedMethodName, params);
			} catch (NoSuchMethodException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return declared;
	}
	
	private static void createMethod(GeneratorAdapter adapter, Class<?> currentClass, Class<?> wantedClass, Method wantedMethod, Class<?> returnType) {
		
		Type implType = getImplType(returnType);

		if(normalToNMS.containsKey(returnType)) {
			adapter.newInstance(implType);
			adapter.dup();
		}
		adapter.loadThis();
		
		adapter.getField(getImplType(currentClass), "handle", GeneratorAdapter.OBJECT_TYPE);
		adapter.checkCast(Type.getType(wantedClass));
		loadArgs(adapter, wantedMethod);
		adapter.invokeVirtual(Type.getType(wantedClass), me.acablade.nmsasm.Method.getMethod(wantedMethod));
		if(normalToNMS.containsKey(returnType)) {
			// new World$NMSImpl(method);
			adapter.invokeConstructor(implType, new me.acablade.nmsasm.Method("<init>", "(Ljava/lang/Object;)V"));
		}
		adapter.returnValue();
		adapter.visitMaxs(20, 20);
		adapter.endMethod();
		
	}
	
	
	private static void createConstructorMethod(ClassWriter writer, Method method, Class<?> wantedClass, Class<?> currentClass) {
		try {
			
			
			Constructor<?> constructor = wantedClass.getConstructor(getParameters(currentClass, method).toArray(new Class<?>[0]));
			GeneratorAdapter adapter = GeneratorAdapter.newMethodGenerator(writer, method.getName(), Type.getMethodDescriptor(method));
	        adapter.loadThis();
	        adapter.visitTypeInsn(Opcodes.NEW, Type.getInternalName(wantedClass));
	        adapter.dup();
	        loadArgsForConstructor(adapter, constructor);
	        adapter.visitMethodInsn(Opcodes.INVOKESPECIAL, Type.getInternalName(wantedClass), "<init>", Type.getConstructorDescriptor(constructor), false);
	        adapter.putField(getImplType(currentClass), "handle", GeneratorAdapter.OBJECT_TYPE);
	        adapter.returnValue();
	        adapter.endMethod();
	        
	        
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static void createGetHandleMethod(ClassWriter writer, Class<?> clazz, Method method) {
		GeneratorAdapter adapter = GeneratorAdapter.newMethodGenerator(writer, "getHandle", Type.getMethodDescriptor(method));
		adapter.loadThis();
		adapter.getField(getImplType(clazz), "handle", GeneratorAdapter.OBJECT_TYPE);
		adapter.returnValue();
		adapter.endMethod();
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
	
	private static void loadArgs(GeneratorAdapter adapter, Method method) {
		int index = adapter.getArgIndex(0);
		for (int i = 0; i < adapter.argumentTypes.length; ++i) {
            Type argumentType = adapter.argumentTypes[i];
            Parameter param = method.getParameters()[i];
            adapter.loadInsn(argumentType, index);
            if(!Type.getType(param.getType()).equals(argumentType)) {
            	
            	adapter.checkCast(Type.getType(param.getType()));
            }
            index += argumentType.getSize();
        }
	}
	
//	private static void loadArgsForConstructor(GeneratorAdapter adapter, Constructor<?> method) {
//		int index = adapter.getArgIndex(0);
//		for (int i = 0; i < adapter.argumentTypes.length; ++i) {
//            Type argumentType = adapter.argumentTypes[i];
//            Parameter param = method.getParameters()[i];
//            adapter.loadInsn(argumentType, index);
//            if(!Type.getType(param.getType()).equals(argumentType)) {
//            	adapter.checkCast(Type.getType(param.getType()));
//            }
//            index += argumentType.getSize();
//        }
//	}
	
	
	private static void loadArgsForConstructor(GeneratorAdapter adapter, Constructor<?> method) {
		int index = adapter.getArgIndex(0);
		for (int i = 0; i < adapter.argumentTypes.length; ++i) {
			
            Type currentType = adapter.argumentTypes[i];
            Parameter wantedParam = method.getParameters()[i];
            adapter.loadInsn(currentType, index);
            Class<?> currentTypeClass = null;
			try {
				currentTypeClass = Class.forName(currentType.getClassName());
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            
            
			Type implType = getImplType(currentTypeClass);
            if(!Type.getType(wantedParam.getType()).equals(currentType)) {
            	if(normalToNMS.get(currentTypeClass).equals(wantedParam.getType())) {
            		adapter.checkCast(implType);
            		adapter.getField(implType, "handle", GeneratorAdapter.OBJECT_TYPE);
            	}
            	adapter.checkCast(Type.getType(wantedParam.getType()));
            }
            
            
            
            index += currentType.getSize();
        }
	}
	
	private static void debugArg(GeneratorAdapter adapter, int index, Type implType) {
		adapter.getStatic(Type.getObjectType("java/lang/System"), "out", Type.getObjectType("java/io/PrintStream"));
		adapter.loadArg(index);
		adapter.checkCast(implType);
		adapter.getField(implType, "handle", GeneratorAdapter.OBJECT_TYPE);
		adapter.invokeVirtual(Type.getObjectType("java/io/PrintStream"), new me.acablade.nmsasm.Method("println", "(Ljava/lang/Object;)V"));
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
			if(classes.size() == 0) throw new NMSException(String.format("%s found zero classes", name));
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
		if(!registered.containsKey(clazz)) throw new NMSException(String.format("Class (%s) is not registered.", clazz.getCanonicalName()));
		Class<T> clz = (Class<T>) registered.get(clazz);
		
		for(Constructor<?> constructor : clz.getConstructors()) {
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
	
	
	private static List<Class<?>> getParameters(Class<?> clazz, Method method){
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
	    
	    Class<?> returnClass = method.getReturnType();
	    
	    if(normalToNMS.containsKey(returnClass)) {
	    	appendDescriptor(normalToNMS.get(returnClass), stringBuilder);
		}else {
			appendDescriptor(returnClass, stringBuilder);
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
