import { Component, OnInit, Inject } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA  } from '@angular/material';
import { FormGroup, FormBuilder } from '@angular/forms';

/**
 * #uncommented
 * @class
 * @extends
 */
@Component({
  selector: 'app-printers',
  templateUrl: './printers.component.html',
  styleUrls: ['./printers.component.css']
})
export class PrintersComponent implements OnInit {

  /** #uncommented */
  printerValue: string;

  /** #uncommented */
  printers = [
  ];

  /**
   * #uncommented
   * @param
   *
   * @return
   */
  constructor() { }

  /**
   * #uncommented
   * @param
   *
   * @return
   */
  ngOnInit(): void {
  }

}
