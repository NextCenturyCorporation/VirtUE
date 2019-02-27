import { Component, EventEmitter, Inject, Input, OnInit, ViewChild } from '@angular/core';
import { FormGroup } from '@angular/forms';
import { MatDialog } from '@angular/material';

import { RouterService } from '../../shared/services/router.service';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material';

import { GenericPageComponent } from '../../shared/abstracts/gen-page/gen-page.component';

import { IndexedObj } from '../../shared/models/indexedObj.model';
import { Printer } from '../../shared/models/printer.model';


/**
* @class
 * #unimplemented class
 *
 */
@Component({
  selector: 'app-printer-modal',
  templateUrl: './printer.modal.html' // ,
  // styleUrls: ['./printer.modal.css']
})
export class PrinterModalComponent implements OnInit {
  title: string = "";

  getPrinter = new EventEmitter();

  printer: Printer = new Printer();

  /**
   * @param dialogRef injected, is a reference to the modal dialog box itself.
   */
  constructor(
      /** injected, is a reference to the modal dialog box itself. */
      public dialogRef: MatDialogRef<PrinterModalComponent>,
      @Inject( MAT_DIALOG_DATA ) data: any
    ) {
      if (data && data['printer']) {
        this.printer = data.printer;
        this.title = "Edit Printer: " + data.printer.name;
      }
      else {
        this.printer = new Printer();
        this.title = "Create New Printer";
      }
  }

  ngOnInit(): void {

  }

  submit(): void {
    this.getPrinter.emit(new Printer(this.printer));
    this.dialogRef.close();
  }

  cancel(): void {
    this.dialogRef.close();
  }
}
