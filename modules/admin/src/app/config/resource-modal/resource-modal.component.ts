import { Component, OnInit, Inject } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA  } from '@angular/material';
import { FormGroup, FormBuilder } from '@angular/forms';

/**
 * #uncommented
 * @class
 * @extends
 */
@Component({
  selector: 'app-resource-modal',
  templateUrl: './resource-modal.component.html',
  styleUrls: ['./resource-modal.component.css']
})
export class ResourceModalComponent implements OnInit {

  /** #uncommented */
  form: FormGroup;

  /** #uncommented */
  resourceValue: string;

  /** #uncommented */
  resources = [
    {value: 'File Share', viewValue: 'File Share'},
    {value: 'Printers', viewValue: 'Printers'}
  ];

  /**
   * #uncommented
   * @param
   *
   * @return
   */
  constructor( public dialogRef: MatDialogRef<ResourceModalComponent> ) {  }


  /**
   * #uncommented
   * @param
   *
   * @return
   */
  saveResource(): void {
    this.dialogRef.close();
  }

  /**
   * #uncommented
   * @param
   *
   * @return
   */
  ngOnInit(): void {
  }

}
