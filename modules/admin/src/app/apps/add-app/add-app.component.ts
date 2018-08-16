import { Component, Inject, OnInit } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';//'@angular/material';

import { Application } from '../../shared/models/application.model';


@Component({
  selector: 'add-app',
  templateUrl: './add-app.component.html'
})
export class AddAppComponent implements OnInit {

  osList = ['LINUX', 'Windows'];
  distroList = ['Debian'];
  selectedOs: string;
  selectedDist: string;

  constructor(
    public dialogRef: MatDialogRef<AddAppComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any
  ) { }

  ngOnInit() {
  }

  onCancel(): void {
    this.dialogRef.close();
    this.selectedOs = null;
    this.selectedDist = null;
  }

  handleFileInput(inputFile : any) {
    console.log("User selected ", inputFile, " for input. Nothing happens with it yet. #TODO");
  }

  onInstall(): void {
    console.log("Install is not implemented.");


    // this.dialogRef.close();
  }
}
