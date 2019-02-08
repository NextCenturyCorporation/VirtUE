
/**
 * @enum
 * The available protocols which a virtue can be permitted to use within a given [[NetworkPermission]]
 * see
 *    withIpProtocol(String ipProtocol)
 *  in
 *    https://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/ec2/model/IpPermission.html
 */
export enum NetworkProtocols {
  TCPIP = "tcp",
  UDP = "udp",
  ICMP = "icmp"
}
