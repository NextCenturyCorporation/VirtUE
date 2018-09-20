

export class Printer {
  /** The ip address of the printer */
  address: string;

  /** A name or description for this printer, to be displayed to the user. */
  info: string = "";
  status: string = "";

  constructor( info: string ) {
    this.info = info;
    this.status = "incorporeal";
  }
}
