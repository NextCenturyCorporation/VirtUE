import { Component, ElementRef, Inject, OnInit, ViewChild } from '@angular/core';
import { FormGroup, FormBuilder } from '@angular/forms';

import { Virtue } from '../../shared/models/virtue.model';
import { BaseUrlService } from '../../shared/services/baseUrl.service';
import { UsersService } from '../../shared/services/users.service';
import { VirtuesService } from '../../shared/services/virtues.service';

import { MatDialogRef, MAT_DIALOG_DATA  } from '@angular/material';

@Component({
  selector: 'app-virtue-modal',
  templateUrl: './virtue-modal.component.html',
  styleUrls: ['./virtue-modal.component.css'],
  providers: [ BaseUrlService, UsersService, VirtuesService ]
})
export class VirtueModalComponent implements OnInit {

  @ViewChild('userVirtue') userVirtueRef: ElementRef;

  form: FormGroup;
  virtues = [];
  userVirtues = [];

  constructor(
    private baseUrlService: BaseUrlService,
    private usersService: UsersService,
    private virtuesService: VirtuesService,
    public dialogRef: MatDialogRef<VirtueModalComponent>,
    @Inject( MAT_DIALOG_DATA ) public data: any
   ) {
    console.log('data', this.data);
  }

  ngOnInit() {
    this.baseUrlService.getBaseUrl().subscribe(res => {
      let awsServer = res[0].aws_server;
      this.getVirtues(awsServer);
    });
  }

  getVirtues(baseUrl: string) {
    this.virtuesService.getVirtues(baseUrl)
      .subscribe(virtues => this.virtues = virtues);
  }

  // onAddVirtues() {
  //   const selVirtue = this.userVirtueRef.nativeElement.value;
  //   // const newVirtue = new UserVirtue(selVirtue);
  //   this.virtuesService.addUserVirtues(selVirtue);
  //   console.log(selVirtue);
  // }

  save() {
    this.dialogRef.close();
  }

}
