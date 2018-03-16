import { Component, Inject, OnInit } from '@angular/core';
import {MatDialog, MatDialogRef, MAT_DIALOG_DATA} from '@angular/material';

@Component({
  selector: 'app-add-vm-app',
  templateUrl: './add-vm-app.component.html'
})
export class AddVmAppComponent implements OnInit {

  constructor(
    public dialogRef: MatDialogRef<AddVmAppComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any
  ) { }

  ngOnInit() {
  }

  onNoClick(): void {
    this.dialogRef.close();
  }

}
