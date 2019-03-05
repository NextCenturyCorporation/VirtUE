import { Component, EventEmitter, Inject, Input, OnInit, ViewChild } from '@angular/core';
import { FormGroup } from '@angular/forms';
import { MatDialog } from '@angular/material';

import { RouterService } from '../../shared/services/router.service';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material';

import { GenericPageComponent } from '../../shared/abstracts/gen-page/gen-page.component';

import { IndexedObj } from '../../shared/models/indexedObj.model';
import { FileSystem } from '../../shared/models/fileSystem.model';



@Component({
  selector: 'app-file-system-modal',
  templateUrl: './fileSystem.modal.html',
  styleUrls: ['./fileSystem.modal.css']
})
export class FileSystemModalComponent implements OnInit {

  title: string = "";

  getFileSystem = new EventEmitter();

  fileSystem: FileSystem = new FileSystem();

  /**
   * see [[GenericPageComponent.constructor]] for notes on inherited parameters
   *
   * @param dialogRef injected, is a reference to the modal dialog box itself.
   */
  constructor(
      public dialogRef: MatDialogRef<FileSystemModalComponent>,
      @Inject( MAT_DIALOG_DATA ) data: any
    ) {
      if (data && data['fileSystem']) {
        this.fileSystem = data.fileSystem;
        this.title = "Edit File System: " + data.fileSystem.name;
      }
      else {
        this.fileSystem = new FileSystem();
        this.title = "Create New File System";
      }
  }

  ngOnInit(): void {

  }

  submit(): void {
    this.getFileSystem.emit(new FileSystem(this.fileSystem));
    this.dialogRef.close();
  }

  cancel(): void {
    this.dialogRef.close();
  }
}
