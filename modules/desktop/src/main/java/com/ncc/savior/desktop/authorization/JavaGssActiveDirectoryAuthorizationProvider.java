package com.ncc.savior.desktop.authorization;

import java.net.URL;
import java.security.Principal;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Set;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.ietf.jgss.GSSContext;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.GSSManager;
import org.ietf.jgss.GSSName;
import org.ietf.jgss.Oid;

/**
 * Active Directory Authorization provider that uses Java GSS libraries. These
 * libraries don't seem to work on Windows so this is a linux/mac
 * implementation.
 *
 */

public class JavaGssActiveDirectoryAuthorizationProvider implements IActiveDirectoryAuthorizationProvider {
	private String loginConfigFilename;

	public JavaGssActiveDirectoryAuthorizationProvider() {
		URL loginConfigURL = JavaGssActiveDirectoryAuthorizationProvider.class.getResource("/ldap.conf");
		loginConfigFilename = loginConfigURL.getFile();
		System.setProperty("java.security.auth.login.config", loginConfigFilename);
	}

	@Override
	public DesktopUser getCurrentUser() {
		LoginContext lc;
		try {
			lc = new LoginContext("SignedOnUserLoginContext");
			// login (effectively populating the Subject)
			lc.login();
			// get the Subject that represents the signed-on user
			// Subject signedOnUserSubject = lc.getSubject();
			throw new RuntimeException("Not Implemented Yet");
		} catch (LoginException e) {
			throw new RuntimeException(e);
		}
	}

	public static void main(String[] args) {
		// System.setProperty("java.security.krb5.realm", "HQ.NEXTCENTURY.COM");
		// System.setProperty("java.security.krb5.kdc", "ws9.hq.nextcentury.com");
		URL loginConfigURL = JavaGssActiveDirectoryAuthorizationProvider.class.getResource("/ldap.conf");
		String loginConfigFilename = loginConfigURL.getFile();
		System.setProperty("java.security.auth.login.config", loginConfigFilename);

		// create a LoginContext based on the entry in the login.conf file
		LoginContext lc;
		try {
			lc = new LoginContext("SignedOnUserLoginContext");
			// login (effectively populating the Subject)
			lc.login();

			// get the Subject that represents the signed-on user
			Subject signedOnUserSubject = lc.getSubject();
			System.out.println("signed-on user subject: " + signedOnUserSubject);
			Set<Principal> principals = signedOnUserSubject.getPrincipals();
			System.out.println("principals: " + principals);

			GSSManager manager = GSSManager.getInstance();

			GSSName serverName = manager.createName("cifs@WS9", GSSName.NT_HOSTBASED_SERVICE);
			String userNameString = principals.iterator().next().getName();
			GSSName clientName = manager.createName(userNameString, GSSName.NT_USER_NAME);

			Oid krb5Oid = new Oid("1.2.840.113554.1.2.2");
			Subject.doAs(signedOnUserSubject, new PrivilegedExceptionAction<Object>() {

				@Override
				public Object run() throws Exception {
					GSSCredential userCredential = manager.createCredential(clientName, 8 * 3600, (Oid) krb5Oid,
							GSSCredential.INITIATE_ONLY);
					GSSContext context = manager.createContext(serverName, krb5Oid, userCredential,
							GSSContext.DEFAULT_LIFETIME);
					context.requestMutualAuth(true);
					System.out.println("successfully obtained credential based on login credential: " + context);
					return true;
				}
			});
		} catch (LoginException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (GSSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (PrivilegedActionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public DesktopUser login(String domain, String username, String password) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public byte[] getCurrentToken(String principal) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void logout() {
		// TODO Auto-generated method stub

	}

	@Override
	public String getAuthorizationTicket(String spn) {
		// TODO Auto-generated method stub
		return null;
	}
}
