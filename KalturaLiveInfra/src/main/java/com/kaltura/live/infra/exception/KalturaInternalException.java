package com.vidiun.live.infra.exception;

public class VidiunInternalException extends RuntimeException {

	private static final long serialVersionUID = -4923449413551219154L;
	
	public VidiunInternalException() { super(); }
	public VidiunInternalException(String message) { super(message); }
	public VidiunInternalException(String message, Throwable cause) { super(message, cause); }
	public VidiunInternalException(Throwable cause) { super(cause); }
	
}
