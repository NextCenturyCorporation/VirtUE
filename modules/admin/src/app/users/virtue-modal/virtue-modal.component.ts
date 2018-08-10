import { Component, ElementRef, EventEmitter, Inject, Input, OnInit, ViewChild } from '@angular/core';
import { FormGroup } from '@angular/forms';

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
  baseUrl: string;

  addVirtues = new EventEmitter();
  appsList = [];
  virtues = [];
  userVirtueIDs = [];

  constructor(
    private appService: ApplicationsService,
    private baseUrlService: BaseUrlService,
    private usersService: UsersService,
    private virtuesService: VirtuesService,
    public dialogRef: MatDialogRef<VirtueModalComponent>,
    @Inject( MAT_DIALOG_DATA ) public data: any
   ) {
    this.userVirtueIDs = data['userVirtueIDs'];
  }

  ngOnInit() {
    this.baseUrlService.getBaseUrl().subscribe(res => {
      let awsServer = res[0].aws_server;
      this.setBaseUrl(awsServer);
      this.getVirtues();
      this.getApps();
    });
    if (this.userVirtueIDs.length > 0) {
      this.userVirtueIDs = this.userVirtueIDs;
    }
  }

  setBaseUrl(url: string) {
    this.baseUrl = url;
  }

  getVirtues() {
    this.virtuesService.getVirtues(this.baseUrl)
      .subscribe(virtues => {
        this.virtues = virtues;
      });
  }

  getApps() {
    this.appService.getAppsList(this.baseUrl).subscribe(apps => this.appsList = apps);
  }

  getAppName(id: string) {
    if (this.appsList.length == 0) {
      return;
    }

    let app = this.appsList.filter(app => app.id === id);

    // console.log(id);

    return app[0].name;
  }

  selectedVirtues(id: string) {
    for (let sel of this.userVirtueIDs) {
      if (sel === id) {
        return true;
      }
    }
    return false;
  }

  cbVirtueList(event, id: string) {
    if (event === true) {
      this.userVirtueIDs.push(id);
      console.log('Added ' + id);
    } else {
      this.removeVm(id);
      console.log('removed ' + id);
    }
  }

  removeVm(id: string) {
    this.userVirtueIDs.splice(this.userVirtueIDs.indexOf(id), 1);
  }

  clearList() {
    this.userVirtueIDs = [];
    this.userVirtueIDs = [];
  }

  onAddVirtues(): void {
    console.log('Selected Virtues: ');
    console.log(this.userVirtueIDs);
    this.addVirtues.emit(this.userVirtueIDs);
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
