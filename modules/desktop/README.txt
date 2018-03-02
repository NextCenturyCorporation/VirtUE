Running Savior Desktop
1. Make sure you have a running instance of virtue-admin
2. Create a a file at ./savior-user.properties
3. Add savior.api.path.base=<server base url> with the appropriate value in the savior-user.properties created above
   a. Typically the base path should be http://localhost:8080/ (note it must have the slash at the end).
4. Run 'gradle run' or com.ncc.savior.desktop.sidebar.SidebarApplication as a java application.
