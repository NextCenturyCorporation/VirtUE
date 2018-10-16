
/**
 * @class
 * This class represnts a printer on the network.
 * TODO functionality for finding, setting up, and storing printers does not yet exist, so this class will likely change.
 */
export class Printer {
  /** The ip address of the printer */
  address: string;

  /** A name or description for this printer, to be displayed to the user. */
  info: string = "";

  /** The printer's status. */
  status: string = "";

  /**
   * @param info see [[info]]
   */
  constructor( info: string ) {
    this.info = info;
    this.status = "incorporeal";
    this.address = "1.2.3.4:" + info
  }
}
