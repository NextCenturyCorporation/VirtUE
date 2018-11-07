import { Component, EventEmitter, Inject, OnInit } from '@angular/core';

import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material';

import { Printer } from '../../shared/models/printer.model';


/**
* @class
 * This class represents a dialoge with a list of selectable colors. When a color is selected, and 'Submit' pressed,
 * the hex value for that color is passed back to the [[VirtueSettingsTabComponent]]
 *
 * Currently this is only used to assign colors to virtues, as an easily distinguishable label.
 *
 * Eventually this should add support for custom colors. Probably not saving them as a perisistent option in this modal though.
 * Could let the user pick a color though by showing a list of Virtues, and letting them pick one to copy.
 */
@Component({
  selector: 'app-printer-modal',
  templateUrl: './printer.modal.html',
  styleUrls: ['./printer.modal.css']
})
export class PrinterModalComponent implements OnInit {

  /** what the calling component subscribes to, in order to receive back the selected color */
  selectPrinter = new EventEmitter<Printer>();

  newPrinter: Printer;

  /**
   * #uncommented
   */
  constructor(
    /** a reference to the dialog itself */
    private dialogRef: MatDialogRef<PrinterModalComponent>,
    /** #I don't think this is usable/necessary */
    @Inject(MAT_DIALOG_DATA) public data: any
  ) {


  }

  /**
   * Don't do anything special on render.
   */
  ngOnInit() {}

  /**
   * if the user clicks cancel. Emits the currently-selected color, and closes the dialog.
   */
  submit(): void {
    this.selectPrinter.emit(this.newPrinter);
    this.dialogRef.close();
  }

  /**
   * if the user clicks cancel. Emits an obvious non-color before closing, so that the subscribed component
   * knows to ignore it and unsubscribe.
   */
  cancel() {
    this.selectPrinter.emit(null);
    this.dialogRef.close();
  }

}
