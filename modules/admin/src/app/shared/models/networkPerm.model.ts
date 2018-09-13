
import { Protocols } from '../enums/enums';

/**
 * #uncommented
 * @class
 * @extends
 */
export class NetworkPermission {

  /** #uncommented */
  host: string = "";

  /** #uncommented */
  protocol: Protocols;

  /** #uncommented */
  localPort: number;

  /** #uncommented */
  remotePort: number;
}
