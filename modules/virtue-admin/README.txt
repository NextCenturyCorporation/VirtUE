Running virtue-admin:
1. Obtain vrtu.pem certificate and place in ./certs/
2. Obtain logon credentials for AWS for a user that has cloud formation creation rights.
3. Put AWS credentials in ./aws.properties in the form:
  accessKey=<access_key>
  secretKey=<secret_access_key>
4. Run 'gradle run' or com.ncc.savior.virtueadmin.VirtueAdminApplication as a java application
5. Provision the database by navigated a browser to: http://localhost:8080/data/templates/preload
