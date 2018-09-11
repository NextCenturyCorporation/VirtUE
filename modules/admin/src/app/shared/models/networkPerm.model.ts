
import { Protocols } from '../enums/enums';

/**
 * #uncommented
 * @class
 * @extends
 */
export class NetworkPermission {
  host: string = "";
  protocol: Protocols;
  localPort: number;
  remotePort: number;
}
