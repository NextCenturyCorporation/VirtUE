Running virtue-admin:
1. Obtain vrtu.pem certificate and place in ./certs/
2. Obtain logon credentials for AWS for a user that has cloud formation creation rights.
3. Put AWS credentials in ./aws.properties in the form:
  accessKey=<access_key>
  secretKey=<secret_access_key>
4. Add necessary properties for dynamic subnets in ./savior-server.properties.  Those properties are the following with sample values:
  #The following two properties establish a CIDR range from which subnets will be created.  
  #Each virtue-admin server needs a dedicated range of IP addresses inside the VPC where it 
  #can create subnets.  This range is created by listing the entry for the first subnet 
  #(virtue.aws.server.subnet.cidrStart) and another entry for the first subnet OUTSIDE of the range
  #(virtue.aws.server.subnet.cidrEnd).  The size of the first block will be used for the size of all subnets.  
  #The end block must be greater than the first block for this to work properly.  
  #It is highly recommended that all virtue-admin servers on the same VPC use unique CIDR block ranges.  
  #These CIDR blocks must be contained within the VPC for the virtue-admin server.
  
  #First CIDR block in range used for dynamic subnets, inclusive.
  virtue.aws.server.subnet.cidrStart=10.1.9.0/28
  #End CIDR block in range used for dynamic subnets, not inclusive.
  virtue.aws.server.subnet.cidrEnd=10.1.10.0/28  
  #AWS Route Table id to assign all dynamic subnets
  virtue.aws.server.subnet.routeTableId=rtb-16314b6c
5. Run 'gradle run' or com.ncc.savior.virtueadmin.VirtueAdminApplication as a java application
6. Provision the database by navigating a browser to: http://localhost:8080/data/templates/preload
