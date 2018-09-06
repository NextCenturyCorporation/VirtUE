
import { Protocols } from '../enums/enums';

export class NetworkPermission {
  host: string = "";
  protocol: Protocols;
  localPort: number;
  remotePort: number;
}
