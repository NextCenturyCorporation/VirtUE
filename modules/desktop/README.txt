Running Savior Desktop
1. Make sure you have a running instance of virtue-admin
2. Create a a file at ./savior-user.properties
3. Add savior.api.path.base=<server base url> with the appropriate value in the savior-user.properties created above
   a. Typically the base path should be http://localhost:8080/ (note it must have the slash at the end).

(Optional)
	4. Build the clipboard jar with 'gradle fatJar' in the clipboard project.
    (*)Ensure you have a java jdk installed, e.g. "sudo apt install openjdk-8-jdk" on linux
	5. Add a full path to the clipboard jar to the property savior.desktop.clipboard.jar in ./savior-user.properties
	   a. on Windows clients, the backslash must be escaped.  I.E. c:\\folder\\clipboard.jar
	6. To enable shortcuts, run 'gradle fatjar' from the desktop directory. This will create a jar named
	 'desktop-0.1.0-SNAPSHOT-all.jar' in the desktop/build/libs directory. Move this jar to the desktop directory

6. Run 'gradle run' or com.ncc.savior.desktop.sidebar.SidebarApplication as a java application.
