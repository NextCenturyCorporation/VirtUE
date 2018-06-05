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

}
