package com.ncc.savior.virtueadmin.security;

import java.security.Principal;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.security.auth.Subject;
import javax.security.auth.kerberos.KerberosPrincipal;
import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.Configuration;
import javax.security.auth.login.LoginContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ietf.jgss.GSSContext;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.GSSManager;
import org.ietf.jgss.GSSName;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.kerberos.authentication.KerberosTicketValidation;
import org.springframework.security.kerberos.authentication.KerberosTicketValidator;
import org.springframework.util.Assert;

public class CollaredSunKerberosJaasTicketValidator implements KerberosTicketValidator, InitializingBean {

	private String servicePrincipal;
	private Resource keyTabLocation;
	private Subject serviceSubject;
	private boolean holdOnToGSSContext;
	private boolean debug = false;
	private static final Log LOG = LogFactory.getLog(CollaredSunKerberosJaasTicketValidator.class);

	@Override
	public KerberosTicketValidation validateTicket(byte[] token) {
		try {
			return Subject.doAs(this.serviceSubject, new KerberosValidateAction(token));
		} catch (PrivilegedActionException e) {
			throw new BadCredentialsException("Kerberos validation not successful", e);
		}
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		Assert.notNull(this.servicePrincipal, "servicePrincipal must be specified");
		Assert.notNull(this.keyTabLocation, "keyTab must be specified");
		if (keyTabLocation instanceof ClassPathResource) {
			LOG.warn(
					"Your keytab is in the classpath. This file needs special protection and shouldn't be in the classpath. JAAS may also not be able to load this file from classpath.");
		}
		String keyTabLocationAsString = this.keyTabLocation.getURL().toExternalForm();
		// We need to remove the file prefix (if there is one), as it is not supported
		// in Java 7 anymore.
		// As Java 6 accepts it with and without the prefix, we don't need to check for
		// Java 7
		if (keyTabLocationAsString.startsWith("file:")) {
			keyTabLocationAsString = keyTabLocationAsString.substring(5);
		}
		LoginConfig loginConfig = new LoginConfig(keyTabLocationAsString, this.servicePrincipal, this.debug);
		Set<Principal> princ = new HashSet<Principal>(1);
		princ.add(new KerberosPrincipal(this.servicePrincipal));
		Subject sub = new Subject(false, princ, new HashSet<Object>(), new HashSet<Object>());
		LoginContext lc = new LoginContext("", sub, null, loginConfig);
		lc.login();
		this.serviceSubject = lc.getSubject();
	}

	/**
	 * The service principal of the application. For web apps this is
	 * <code>HTTP/full-qualified-domain-name@DOMAIN</code>. The keytab must contain
	 * the key for this principal.
	 *
	 * @param servicePrincipal service principal to use
	 * @see #setKeyTabLocation(Resource)
	 */
	public void setServicePrincipal(String servicePrincipal) {
		this.servicePrincipal = servicePrincipal;
	}

	/**
	 * <p>
	 * The location of the keytab. You can use the normale Spring Resource prefixes
	 * like <code>file:</code> or <code>classpath:</code>, but as the file is later
	 * on read by JAAS, we cannot guarantee that <code>classpath</code> works in
	 * every environment, esp. not in Java EE application servers. You should use
	 * <code>file:</code> there.
	 *
	 * This file also needs special protection, which is another reason to not
	 * include it in the classpath but rather use <code>file:/etc/http.keytab</code>
	 * for example.
	 *
	 * @param keyTabLocation The location where the keytab resides
	 */
	public void setKeyTabLocation(Resource keyTabLocation) {
		this.keyTabLocation = keyTabLocation;
	}

	/**
	 * Enables the debug mode of the JAAS Kerberos login module.
	 *
	 * @param debug default is false
	 */
	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	/**
	 * Determines whether to hold on to the {@link GSSContext GSS security context}
	 * or otherwise {@link GSSContext#dispose() dispose} of it immediately (the
	 * default behaviour).
	 * <p>
	 * Holding on to the GSS context allows decrypt and encrypt operations for
	 * subsequent interactions with the principal.
	 *
	 * @param holdOnToGSSContext true if should hold on to context
	 */
	public void setHoldOnToGSSContext(boolean holdOnToGSSContext) {
		this.holdOnToGSSContext = holdOnToGSSContext;
	}

	/**
	 * This class is needed, because the validation must run with previously
	 * generated JAAS subject which belongs to the service principal and was loaded
	 * out of the keytab during startup.
	 */
	private class KerberosValidateAction implements PrivilegedExceptionAction<KerberosTicketValidation> {
		byte[] kerberosTicket;

		public KerberosValidateAction(byte[] kerberosTicket) {
			this.kerberosTicket = kerberosTicket;
		}

		@Override
		public KerberosTicketValidation run() throws Exception {
			byte[] responseToken = new byte[0];
			GSSName gssName = null;
			GSSContext context = GSSManager.getInstance().createContext((GSSCredential) null);
			boolean first = true;
			while (!context.isEstablished()) {
				if (first) {
					kerberosTicket = tweakJdkRegression(kerberosTicket);
				}
				responseToken = context.acceptSecContext(kerberosTicket, 0, kerberosTicket.length);
				gssName = context.getSrcName();
				if (gssName == null) {
					throw new BadCredentialsException("GSSContext name of the context initiator is null");
				}
				first = false;
			}
			GSSCredential delegationCredential = null;
			if (context.getCredDelegState()) {
				delegationCredential = context.getDelegCred();
			} else {
				LOG.warn(
						"Did not get delegated credential - have you correctly configured the service principal for delegation?");
			}
			if (!holdOnToGSSContext) {
				context.dispose();
			}
			return new CollaredKerberosTicketValidation(
					new KerberosTicketValidation(gssName.toString(), servicePrincipal, responseToken, context),
					delegationCredential);
		}
	}

	/**
	 * Normally you need a JAAS config file in order to use the JAAS Kerberos Login
	 * Module, with this class it is not needed and you can have different
	 * configurations in one JVM.
	 */
	private static class LoginConfig extends Configuration {
		private String keyTabLocation;
		private String servicePrincipalName;
		private boolean debug;

		public LoginConfig(String keyTabLocation, String servicePrincipalName, boolean debug) {
			this.keyTabLocation = keyTabLocation;
			this.servicePrincipalName = servicePrincipalName;
			this.debug = debug;
		}

		@Override
		public AppConfigurationEntry[] getAppConfigurationEntry(String name) {
			HashMap<String, String> options = new HashMap<String, String>();
			options.put("useKeyTab", "true");
			options.put("keyTab", this.keyTabLocation);
			options.put("principal", this.servicePrincipalName);
			options.put("storeKey", "true");
			options.put("doNotPrompt", "true");
			if (this.debug) {
				options.put("debug", "true");
			}
			options.put("isInitiator", "true");
			options.put("refreshKrb5Config", "true");

			return new AppConfigurationEntry[] {
					new AppConfigurationEntry("com.sun.security.auth.module.Krb5LoginModule",
							AppConfigurationEntry.LoginModuleControlFlag.REQUIRED, options), };
		}

	}

	private static byte[] tweakJdkRegression(byte[] token) throws GSSException {

//    	Due to regression in 8u40/8u45 described in
//    	https://bugs.openjdk.java.net/browse/JDK-8078439
//    	try to tweak token package if it looks like it has
//    	OID's in wrong order
//
//      0000: 60 82 06 5C 06 06 2B 06   01 05 05 02 A0 82 06 50
//      0010: 30 82 06 4C A0 30 30 2E  |06 09 2A 86 48 82 F7 12
//      0020: 01 02 02|06 09 2A 86 48   86 F7 12 01 02 02 06|0A
//      0030: 2B 06 01 04 01 82 37 02   02 1E 06 0A 2B 06 01 04
//      0040: 01 82 37 02 02 0A A2 82   06 16 04 82 06 12 60 82
//
//    	In above package first token is in position 24 and second
//    	in 35 with both having size 11.
//
//    	We simple check if we have these two in this order and swap
//
//    	Below code would create two arrays, lets just create that
//    	manually because it doesn't change
//      Oid GSS_KRB5_MECH_OID = new Oid("1.2.840.113554.1.2.2");
//      Oid MS_KRB5_MECH_OID = new Oid("1.2.840.48018.1.2.2");
//		byte[] der1 = GSS_KRB5_MECH_OID.getDER();
//		byte[] der2 = MS_KRB5_MECH_OID.getDER();

//		0000: 06 09 2A 86 48 86 F7 12   01 02 02
//		0000: 06 09 2A 86 48 82 F7 12   01 02 02

		if (token == null || token.length < 48) {
			return token;
		}

		int[] toCheck = new int[] { 0x06, 0x09, 0x2A, 0x86, 0x48, 0x82, 0xF7, 0x12, 0x01, 0x02, 0x02, 0x06, 0x09, 0x2A,
				0x86, 0x48, 0x86, 0xF7, 0x12, 0x01, 0x02, 0x02 };

		for (int i = 0; i < 22; i++) {
			if ((byte) toCheck[i] != token[i + 24]) {
				return token;
			}
		}

		byte[] nt = new byte[token.length];
		System.arraycopy(token, 0, nt, 0, 24);
		System.arraycopy(token, 35, nt, 24, 11);
		System.arraycopy(token, 24, nt, 35, 11);
		System.arraycopy(token, 46, nt, 46, token.length - 24 - 11 - 11);
		return nt;
	}

}
