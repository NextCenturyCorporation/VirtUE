This is the code for assembling the Windows Display Server.

To make a new WDS image, run

   packer build wds.json

and wait about 8 minutes. When it's done it will print the ID of the
new AMI (e.g., ami-0f0b3135df8f25772).

You may want to update the property file with the new AMI:
modules/virtue-admin/src/main/resources/savior-server.properties

The property is:
virtue.winDisplay.aws.ami
