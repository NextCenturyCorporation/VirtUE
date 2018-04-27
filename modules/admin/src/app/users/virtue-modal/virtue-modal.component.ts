import { Component, ElementRef, EventEmitter, Inject, Input, OnInit, ViewChild } from '@angular/core';
import { FormGroup, FormBuilder } from '@angular/forms';

import { Virtue } from '../../shared/models/virtue.model';
import { ApplicationsService } from '../../shared/services/applications.service';
import { BaseUrlService } from '../../shared/services/baseUrl.service';
import { UsersService } from '../../shared/services/users.service';
import { VirtuesService } from '../../shared/services/virtues.service';

import { MatDialogRef, MAT_DIALOG_DATA  } from '@angular/material';

@Component({
  selector: 'app-virtue-modal',
  templateUrl: './virtue-modal.component.html',
  providers: [ ApplicationsService, BaseUrlService, UsersService, VirtuesService ]
})
export class VirtueModalComponent implements OnInit {
  @Input() virtueInput: Virtue;

  @ViewChild('userVirtue') userVirtueRef: ElementRef;

  form: FormGroup;

  addVirtues = new EventEmitter();
  virtues = [];
  selVirtues = [];
  storedVirtues = [];

  constructor(
    private appService: ApplicationsService,
    private baseUrlService: BaseUrlService,
    private usersService: UsersService,
    private virtuesService: VirtuesService,
    public dialogRef: MatDialogRef<VirtueModalComponent>,
    @Inject( MAT_DIALOG_DATA ) public data: any
   ) {  }

  ngOnInit() {
    this.baseUrlService.getBaseUrl().subscribe(res => {
      let awsServer = res[0].aws_server;
      this.getVirtues(awsServer);
    });
  }

  getVirtues(baseUrl: string) {
    this.virtuesService.getVirtues(baseUrl)
      .subscribe(virtues => {
        this.virtues = virtues;
      });
  }

  selectedVirtues(id: string) {
    if (this.storedVirtues.length > 0) {
      for (let sel of this.storedVirtues) {
        if (sel === id) {
          return true;
        }
      }
    } else {
      return false;
    }
  }

  cbVirtueList(event, id: string, index: number) {
    if (event === true) {
      this.selVirtues.push(id);
    } else {
      this.removeVm(id, index);
    }
  }

  removeVm(id: string, index: number) {
    this.selVirtues.splice(this.selVirtues.indexOf(id), 1);
  }

  clearList() {
    this.selVirtues = [];
    this.storedVirtues = [];
  }

  onAddVirtues(): void {
    // if (this.storedVirtues.length > 0) {
    //   this.selVmsList = this.storedVirtues;
    // }
    this.addVirtues.emit(this.selVirtues);
    this.clearList();
    this.dialogRef.close();
  }

  cancelModal() {
    this.clearList();
    this.dialogRef.close();
  }

  save() {
    this.dialogRef.close();
  }

}
