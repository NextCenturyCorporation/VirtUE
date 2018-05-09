import { Component, EventEmitter, OnInit, Inject } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA  } from '@angular/material';
import { FormGroup, FormBuilder } from '@angular/forms';

@Component({
  selector: 'app-dialogs',
  templateUrl: './dialogs.component.html',
  styleUrls: ['./dialogs.component.css']
})
export class DialogsComponent implements OnInit {

  form: FormGroup;
  dialogType: string;
  dialogCategory: string;
  dialogDescription: string;
  dialogId: string;
  dialogEmitter = new EventEmitter();

  constructor( public dialogRef: MatDialogRef<DialogsComponent>, @Inject( MAT_DIALOG_DATA ) public data: any ) {
    // console.log(this.data);
    this.dialogType = this.data['dialogType'];
    this.dialogCategory = this.data['dialogCategory'];
    this.dialogDescription = this.data['dialogDescription'];
    this.dialogId = this.data['dialogId'];
  }

  confirmSelection() {
    if (this.dialogType === 'delete') {
      this.dialogEmitter.emit(this.dialogId);
    } 
    this.dialogRef.close();
  }

  ngOnInit() {
  }
}
