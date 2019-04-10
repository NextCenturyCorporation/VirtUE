aws s3 cp s3://${bucket}/s3download-0.1.0-SNAPSHOT-all.jar ~
mv s3download-*.jar s3download.jar
sudo yum remove -y java-1.7.0-openjdk.x86_64
sudo yum install -y java-1.8.0-openjdk.x86_64