import { Component, EventEmitter, OnInit, Inject, Output } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA  } from '@angular/material';
import { FormGroup } from '@angular/forms';

@Component({
  selector: 'app-dialogs',
  templateUrl: './dialogs.component.html'
})
export class DialogsComponent implements OnInit {

  form: FormGroup;
  dialogType: string;
  dialogCategory: string;
  dialogDescription: string;
  dialogId: string;
  @Output() dialogEmitter: EventEmitter<any> = new EventEmitter();

  constructor( public dialogRef: MatDialogRef<DialogsComponent>, @Inject( MAT_DIALOG_DATA ) public data: any ) {
    // console.log(this.data);
    this.dialogType = this.data['dialogType'];
    this.dialogCategory = this.data['dialogCategory'];
    this.dialogDescription = this.data['dialogDescription'];
    this.dialogId = this.data['dialogId'];
  }

  ngOnInit() {
  }

  confirmSelection() {
    if (this.dialogType === 'delete') {
      this.dialogEmitter.emit(this.dialogId);
    }
    this.dialogRef.close();
  }

  submit() {
    if (this.dialogType === 'delete') {
      // this.dialogEmitter.emit(this.dialogId); //What's this supposed to do?
      this.dialogEmitter.emit('adfg');
    }
    this.dialogRef.close();
  }
  //
  cancel() {
    if (this.dialogType === 'delete') {
      // this.dialogEmitter.emit(this.dialogId); //What's this supposed to do?
      this.dialogEmitter.emit(0);
    }
    this.dialogRef.close();
  }
}
