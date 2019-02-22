import { DictList } from './dictionary.model';
import { IndexedObj } from './indexedObj.model';
import { Toggleable } from './toggleable.interface';
import { DatasetNames } from '../abstracts/gen-data-page/datasetNames.enum';

/**
 * @class
 * This class represnts a printer on the network. These are set up in the global settings' printer config page, and then can be added
 * to any Virtue.
 */
export class Printer extends IndexedObj implements Toggleable {

  /** the id given by the backend */
  id: string;

  /** The printer's name */
  name: string;

  /** The ip address of the printer */
  address: string;

  /** A name or description for this printer, to be displayed to the user. */
  info: string;

  /**
   * The printer's status. TODO may be an enum, pending how printers get connected. Only enum if theres a common interface with common
   * status codes. Else a string, set by the printer's status codes.
   */
  status: string;

  /** Whether the printer is enabled or disabled. */
  enabled: boolean = false;

  constructor( printer?: {id?: string, name: string, info?: string, status: string, address: string, enabled: boolean} ) {
    super();

    if (printer) {
      this.id = printer.id;
      this.name = printer.name;
      this.info = printer.info;
      this.status = printer.status;
      this.address = printer.address;
      this.enabled = printer.enabled;
    }
  }

  getID(): string {
    return this.id;
  }

  getDatasetName(): string {
    return DatasetNames.PRINTERS;
  }
}
