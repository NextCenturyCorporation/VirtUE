Running virtue-admin:
1. Obtain vrtu.pem certificate and place in ./certs/
2. Obtain logon credentials for AWS for a user that has cloud formation creation rights.
3. Put AWS credentials in ./aws.properties in the form:
  [virtue]
  aws_access_key_id=<access_key>
  aws_secret_access_key=<secret_access_key>
4. Run 'gradle run' or com.ncc.savior.virtueadmin.VirtueAdminApplication as a java application

