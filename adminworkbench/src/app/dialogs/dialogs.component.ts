import { Component, OnInit } from '@angular/core';
import { MatDialogRef } from '@angular/material';
import { FormGroup, FormBuilder } from '@angular/forms';

@Component({
  selector: 'app-dialogs',
  templateUrl: './dialogs.component.html',
  styleUrls: ['./dialogs.component.css']
})
export class DialogsComponent implements OnInit {

  form: FormGroup;

  constructor(
    //private formBuilder: FormBuilder,
    private dialogRef: MatDialogRef<DialogsComponent>
  ) {}

  ngOnInit() {

  }
  confirmSelection() {
    this.dialogRef.close();
  }
}
