package com.github.mcfongtw.jni;

import java.io.Serializable;

public class NativeObject implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 317537417537509112L;
	
	private long nativePtr;
	
	public NativeObject() {
		
	}
	
	private native void init_object();
	
}
