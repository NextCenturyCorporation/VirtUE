import { Component, OnInit, Inject } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA  } from '@angular/material';
import { FormGroup, FormBuilder } from '@angular/forms';

@Component({
  selector: 'app-file-share',
  templateUrl: './file-share.component.html',
  styleUrls: ['./file-share.component.css']
})
export class FileShareComponent implements OnInit {

  fsValue: string;
  fileShareTypes = [
    {value: 'NFS', viewValue: 'NFS'},
    {value: 'CIFS', viewValue: 'Windows (CIFS)'},
  ];

  constructor() { }

  ngOnInit() {
  }

}
