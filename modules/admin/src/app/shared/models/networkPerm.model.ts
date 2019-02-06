
import { NetworkProtocols } from '../../virtues/protocols.enum';

/**
 * @class
 * This class represents a whitelisted network connection.
 * TODO not clear if this is for out-bound, in-bound, or two-way connections
 */
export class NetworkPermission {

  /** ID of the security group.
   * Currently!!, the back end looks this up using the templateID, and then overwrites whatever was in this field.*/
  private securityGroupId: string = "";

  /** Virtue template ID that this permission pertains to. */
  private templateId: string = ""; // determined automatically

  /** Human readable description of the purpose of this security group. */
  public description: string = ""; // optional, user-supplied

  /** True if the security group pertains to connections coming into into the virtue, false if the connection is outgoing.*/
  public ingress: boolean = false; // user-supplied

	/** Port the connection came from. */
	public fromPort: number; // user-supplied

	/** Port the connection came to. */
	public toPort: number; // user-supplied

	/** Protocol for.  Usually TCP or UDP. */
	public ipProtocol: NetworkProtocols = NetworkProtocols.TCPIP; // user-supplied

  /** CIDR block of that the permission should pertain to. */
  public cidrIp: string; // user-supplied

  constructor(netPerm?) {
    if (netPerm) {
      this.templateId = netPerm.templateId;

      if ('securityGroupId' in netPerm) { // if we're loading an existing networkPerm
        this.securityGroupId = netPerm.securityGroupId;
        this.description = netPerm.description;
        this.ingress = netPerm.ingress;
        this.fromPort = netPerm.fromPort;
        this.toPort = netPerm.toPort;
        this.ipProtocol = netPerm.ipProtocol;
        this.cidrIp = netPerm.cidrIp;
      }
    }
  }

  equals(obj: any): boolean {
    if (!obj) {
      return false;
    }
    // remember instanceof just checks the prototype.
    // If any of these don't exist, that's ok.
    return (obj.templateId === this.templateId)
        && (obj.ingress === this.ingress)
        && (obj.fromPort === this.fromPort)
        && (obj.toPort === this.toPort)
        && (obj.ipProtocol === this.ipProtocol)
        && (obj.cidrIp === this.cidrIp);
  }

  /**
   * Check a particular network permission - all 4 of its fields should be filled out and valid.
   * @return true if all fields are valid.
   */
  checkValid(): boolean {

    // first make sure that the ports aren't 0, because checking !port will be true
    // if port === 0. Which would make the wrong error message appear.
    if (
        this.fromPort === 0
        || this.toPort === 0
        ) {
      console.log("Ports on network permissions must be greater than zero.");
      return false;
    }

    if ( !(this.toPort || this.fromPort) ) {
      console.log("Both local and remote ports must be given.");
      return false;
    }

    if ( !this.ipProtocol ) {
      console.log("Please select a protocol.");
      return false;
    }

    if ( !this.cidrIp ) {
      console.log("Destination CIDR IP must not be blank.");
      return false;
    }

    if (
      this.fromPort < 0
       || this.toPort < 0) {
      console.log("Ports must be greater than zero.");
      return false;
    }

    return true;
  }
}
