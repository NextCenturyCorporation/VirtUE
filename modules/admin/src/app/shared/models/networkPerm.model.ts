
import { Protocols } from '../enums/enums';

export class NetworkPermission {

  /** The address of the network this permission is for */
  destination: string = "";

  /** What connection protocol must be used */
  protocol: Protocols;
  localPort: number;
  remotePort: number;
}
