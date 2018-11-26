import { Component, EventEmitter, Inject, OnInit } from '@angular/core';

import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material';

import { Printer } from '../../shared/models/printer.model';


/**
* @class
 * #unimplemented class
 *
 */
@Component({
  selector: 'app-printer-modal',
  templateUrl: './printer.modal.html',
  styleUrls: ['./printer.modal.css']
})
export class PrinterModalComponent implements OnInit {

  /** what the calling component subscribes to, in order to receive back the selected color */
  createPrinter = new EventEmitter<Printer>();

  newPrinter: Printer;

  /**
   * #uncommented
   */
  constructor(
    /** a reference to the dialog box itself */
    private dialogRef: MatDialogRef<PrinterModalComponent>,
    /** #I don't think this is usable/necessary */
    @Inject(MAT_DIALOG_DATA) public data: any
  ) {


  }

  /**
   * Don't do anything special on render.
   */
  ngOnInit() {}


  submit(): void {
    this.createPrinter.emit(this.newPrinter);
    this.dialogRef.close();
  }

  cancel() {
    this.dialogRef.close();
  }

}
