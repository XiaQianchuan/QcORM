package com.xqc.qcorm.util;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

public class LoggerUtil {
	
	public static Logger log = null;
	
	//初始化日志记录器
	static {
		BasicConfigurator.configure();
		log = Logger.getRootLogger();
	}
}