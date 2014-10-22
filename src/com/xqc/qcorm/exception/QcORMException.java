package com.xqc.qcorm.exception;

public class QcORMException extends Exception {

	private static final long serialVersionUID = 1L;

	public QcORMException() {
		super();
	}
	
	public QcORMException(String msg) {
		super(msg);
	}
	
	public QcORMException(String message, Throwable cause) {
        super(message, cause);
    }
	
	public QcORMException(Throwable cause) {
		super(cause);
	}
}
