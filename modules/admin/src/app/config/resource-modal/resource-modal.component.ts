import { Component, OnInit, Inject } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA  } from '@angular/material';
import { FormGroup, FormBuilder } from '@angular/forms';

@Component({
  selector: 'app-resource-modal',
  templateUrl: './resource-modal.component.html',
  styleUrls: ['./resource-modal.component.css']
})
export class ResourceModalComponent implements OnInit {

  form: FormGroup;
  resourceValue: string;
  resources = [
    {value: 'File Share', viewValue: 'File Share'},
    {value: 'Printers', viewValue: 'Printers'}
  ];

  constructor( public dialogRef: MatDialogRef<ResourceModalComponent> ) {  }


  saveResource() {
    this.dialogRef.close();
  }

  ngOnInit() {
  }

}
