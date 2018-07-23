import { Component, Inject, OnInit } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA} from '@angular/material';

@Component({
  selector: 'app-add-vm-app',
  templateUrl: './add-vm-app.component.html'
})
export class AddVmAppComponent implements OnInit {

  osList = ['LINUX', 'Windows'];
  distroList = ['Debian'];
  selectedOs: string;
  selectedDist: string;

  constructor(
    public dialogRef: MatDialogRef<AddVmAppComponent>,
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
