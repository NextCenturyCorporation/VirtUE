Running virtue-admin:
1. Obtain vrtu.pem certificate and place in ./certs/
2. Obtain logon credentials for AWS for a user that has cloud formation creation rights.
3. Put AWS credentials in ./aws.properties in the form:
  accessKey=<access_key>
  secretKey=<secret_access_key>
4. Add necessary properties for dynamic subnets in ./savior-server.properties.  Those properties are the following with sample values:
  #First cidr block in range used for dynamic subnets, inclusive.
  virtue.aws.server.subnet.cidrStart=10.0.8.0/24
  #End cidr block in range used for dynamic subnets, not inclusive.
  virtue.aws.server.subnet.cidrEnd=10.0.1.0/24  
  #AWS Route Table id to assign all dynamic subnets
  virtue.aws.server.subnet.routeTableId=rtb-16314b6c
5. Run 'gradle run' or com.ncc.savior.virtueadmin.VirtueAdminApplication as a java application
6. Provision the database by navigated a browser to: http://localhost:8080/data/templates/preload
