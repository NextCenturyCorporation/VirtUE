import { Component, OnInit } from '@angular/core';
import { MatDialogRef } from '@angular/material';
import { FormGroup, FormBuilder } from '@angular/forms';

@Component({
  selector: 'app-vm-modal',
  templateUrl: './vm-modal.component.html',
  styleUrls: ['./vm-modal.component.css']
})
export class VmModalComponent implements OnInit {

  constructor(
    //private formBuilder: FormBuilder,
    private dialogRef: MatDialogRef<VmModalComponent>
  ) {}

  ngOnInit() {
  }

  saveVMList() {
    this.dialogRef.close();
  }

}
