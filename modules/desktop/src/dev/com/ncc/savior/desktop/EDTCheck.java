package com.ncc.savior.desktop;

import java.awt.EventQueue;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;

/**
 * From
 * https://thejavacodemonkey.blogspot.com/2007/08/using-aspectj-to-detect-violations-of.html
 *
 * @author clong
 *
 */
@Aspect
public class EDTCheck {

	@Pointcut("call (* javax.swing..*+.*(..)) || " + "call (javax.swing..*+.new(..))")
	public void swingMethods() {
	}

	@Pointcut("call (* javax.swing..*+.add*Listener(..)) || " + "call (* javax.swing..*+.remove*Listener(..)) || "
			+ "call (void javax.swing.JComponent+.setText(java.lang.String)) ||"
			+ "call (void javax.swing.SwingUtilities.invoke*(java.lang.Runnable))")
	public void safeMethods() {
	}

	@Before("swingMethods() && !safeMethods()")
	public void checkCallingThread(JoinPoint.StaticPart thisJoinPointStatic) {
		if (!EventQueue.isDispatchThread()) {
			System.out.println("Swing single thread rule violation: " + thisJoinPointStatic);
			Thread.dumpStack();
		}
	}

}
