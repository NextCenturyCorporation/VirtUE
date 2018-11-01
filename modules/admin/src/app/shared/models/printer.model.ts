import { DictList } from './dictionary.model';
import { IndexedObj } from './indexedObj.model';

/**
 * @class
 * This class represnts a printer on the network.
 * TODO functionality for finding, setting up, and storing printers does not yet exist, so this class will likely change.
 */
export class Printer extends IndexedObj {

  /** the id given by the backend */
  id: string;

  /** The ip address of the printer */
  address: string;

  /** A name or description for this printer, to be displayed to the user. */
  info: string;

  /** The printer's status. */
  status: string;

  /** Whether the printer is enabled or disabled. */
  enabled: boolean;

  /**
   * @param info see [[info]]
   */
  constructor( input: string | Printer ) {


    if (input instanceof Printer) {
      super(input.id);
      this.info = input.info;
      this.status = input.status;
      this.address = input.address;
      this.enabled = input.enabled;
    }
    else {
      super("totallyUniqueID");
      this.info = info;
      this.status = "incorporeal";
      this.address = "1.2.3.4:" + info;
      this.enabled = true;
    }
  }

  /** #uncommented */
  getID(): string {
    return this.id;
  }

  /** doesn't depend on anything else, and so nothing needs to be built */
  buildAttributes(childDatasets: DictList<(DictList<IndexedObj>)> ): void {}
}
