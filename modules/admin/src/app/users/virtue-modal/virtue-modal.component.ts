import { Component, OnInit, Inject } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA  } from '@angular/material';
import { FormGroup, FormBuilder } from '@angular/forms';

@Component({
  selector: 'app-virtue-modal',
  templateUrl: './virtue-modal.component.html',
  styleUrls: ['./virtue-modal.component.css']
})
export class VirtueModalComponent implements OnInit {

  form: FormGroup;
  virtues = [
    'Microsoft Office',
    'Microsoft Outlook',
    'Adobe Suite',
    'Developer Bundle',
    'Admin Bundle',
    'Project Management'
  ];

  constructor( public dialogRef: MatDialogRef<VirtueModalComponent>, @Inject( MAT_DIALOG_DATA ) public data: any ) {
    console.log('data', this.data);
  }

  save() {
    this.dialogRef.close();
  }

  ngOnInit() {
  }

}
