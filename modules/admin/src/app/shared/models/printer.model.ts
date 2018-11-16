import { DictList } from './dictionary.model';
import { IndexedObj } from './indexedObj.model';
import { Toggleable } from './toggleable.interface';
import { Subdomains } from '../services/subdomains.enum';

/**
 * @class
 * This class represnts a printer on the network.
 * TODO functionality for finding, setting up, and storing printers does not yet exist, so this class will likely change.
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

  /** The printer's status. */
  status: string;

  /** Whether the printer is enabled or disabled. */
  enabled: boolean;

  /**
   * @param info see [[info]]
   */
  constructor( printer?: {id?: string, name: string, info?: string, status: string, address: string, enabled: boolean} ) {

    super();
    this.id = printer.id;
    this.name = printer.name;
    this.info = printer.info;
    this.status = printer.status;
    this.address = printer.address;
    this.enabled = printer.enabled;

  }

  /** #uncommented */
  getID(): string {
    return this.id;
  }
  /**
   * @return the PRINTERS subdomain
   */
  getSubdomain(): string {
    return Subdomains.PRINTERS;
  }
}
