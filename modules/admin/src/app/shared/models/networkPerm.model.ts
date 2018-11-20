
import { NetworkProtocols } from '../../virtues/protocols.enum';

/**
 * @class
 * This class represents a whitelisted network connection.
 * TODO not clear if this is for out-bound, in-bound, or two-way connections
 */
export class NetworkPermission {

  /** The address of the network this permission is for */
  destination: string = "";

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

  equals(obj: any): boolean {
    if (!obj) {
      return false;
    }
    // remember instanceof just checks the prototype.
    // If any of these don't exist, that's ok.
    return (obj.destination === this.destination)
        && (obj.protocol === this.protocol)
        && (obj.localPort === this.localPort)
        && (obj.remotePort === this.remotePort);
  }

}
