import { Component, Inject, OnInit, EventEmitter } from '@angular/core';
import { MatDialogRef, MatDialogModule, MAT_DIALOG_DATA } from '@angular/material/dialog';

import { Application } from '../../shared/models/application.model';

@Component({
  selector: 'app-add-app', // thanks linter, this looks lovely.
  templateUrl: './add-app.component.html'
})
/**
 * Note: this page is essentially unimplemented.
 * #TODO
 */
export class AddAppComponent implements OnInit {


  osList = ['LINUX', 'Windows'];
  distroList = ['Debian'];
  selectedOs: string;
  selectedDist: string;

  constructor(
    public dialogRef: MatDialogRef<AddAppComponent>,
    /**
     * Data is expected to hold _______
     */
    @Inject(MAT_DIALOG_DATA) public data: any
  ) {

  }

  /**
   * #uncommented
   * @param
   *
   * @return
   */
  ngOnInit() {
  }

  /**
   * #uncommented
   * @param
   *
   * @return
   */
  onCancel() {
    this.dialogRef.close();
    return "bob";
  }

  /**
   * #uncommented
   * @param
   *
   * @return
   */
  handleFileInput(inputFile: any) {
    console.log("User selected ", inputFile, " for input. Nothing happens with it yet.");
  }

  /**
   * #uncommented
   * @param
   *
   * @return
   */
  onInstall() {
    console.log("Install is not implemented.");


    this.dialogRef.close();
    return "test";
  }
}
