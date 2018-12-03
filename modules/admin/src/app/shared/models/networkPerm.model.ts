
import { NetworkProtocols } from '../../virtues/protocols.enum';

/**
 * @class
 * This class represents a whitelisted network connection.
 * TODO not clear if this is for out-bound, in-bound, or two-way connections
 */
export class NetworkPermission {

  /** The address of the network this permission is for */
  host: string = "";

  /** What connection protocol must be used */
  protocol: NetworkProtocols;

  /** What specific local port can make this connection.
   * I thought these were chosen randomly on the connecting machine -
   * TODO see virtue notes
   * Oh or maybe it allows two-way comms. This is the port which that resource can connect to?
   * #uncommented
   */
  localPort: number;

  /** The port to connect to on the desination */
  remotePort: number;

  constructor(netPerm?) {
    if (netPerm) {
      this.host = netPerm.host;
      this.protocol = NetworkProtocols.TCPIP; // netPerm.protocol; // TODO FIXME
      this.localPort = netPerm.localPort;
      this.remotePort = netPerm.remotePort;
    }
  }

  equals(obj: any): boolean {
    if (!obj) {
      return false;
    }
    // remember instanceof just checks the prototype.
    // If any of these don't exist, that's ok.
    return (obj.host === this.host)
        && (obj.protocol === this.protocol)
        && (obj.localPort === this.localPort)
        && (obj.remotePort === this.remotePort);
  }

  /**
   * Check a particular network permission - all 4 of its fields should be filled out and valid.
   * @return true if all fields are valid.
   */
  checkValid(): boolean {

    // instead of checking  '<=='
    // first make sure that the ports aren't 0, because checking !port will be true
    // if port === 0. Which would make the wrong error message appear.
    if (this.localPort === 0 || this.remotePort === 0) {
      console.log("Ports on network permissions must be greater than zero.");
      return false;
    }

    if ( !this.host       || !this.protocol
      || !this.localPort  || !this.remotePort ) {
      console.log("Network permission fields cannot be blank");
      return false;
    }

    // if ( !(this.localPort instanceof Number) || !(this.remotePort instanceof Number) ) {
    //   console.log("Local and Remote ports must be numbers.");
    //   return false;
    // }

    if (this.localPort < 0 || this.remotePort < 0) {
      console.log("Ports on network permissions must be greater than zero.");
      return false;
    }

    return true;
  }
}
