import { Component, OnInit, Inject } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA  } from '@angular/material';
import { FormGroup, FormBuilder } from '@angular/forms';

@Component({
  selector: 'app-dialogs',
  templateUrl: './dialogs.component.html',
  styleUrls: ['./dialogs.component.css']
})
export class DialogsComponent implements OnInit {

  form: FormGroup;

  constructor( public dialogRef: MatDialogRef<DialogsComponent>, @Inject( MAT_DIALOG_DATA ) public data: any ) {
    console.log('data', this.data);
  }

  confirmSelection() {
    this.dialogRef.close();
  }

  ngOnInit() {
  }
}
