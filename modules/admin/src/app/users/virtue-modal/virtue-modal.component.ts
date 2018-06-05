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
  selVirtues = [];
  storedVirtues = [];

  constructor(
    private appService: ApplicationsService,
    private baseUrlService: BaseUrlService,
    private usersService: UsersService,
    private virtuesService: VirtuesService,
    public dialogRef: MatDialogRef<VirtueModalComponent>,
    @Inject( MAT_DIALOG_DATA ) public data: any
   ) {
    this.storedVirtues = data['storedVirtues'];
  }

  ngOnInit() {
    this.baseUrlService.getBaseUrl().subscribe(res => {
      let awsServer = res[0].aws_server;
      this.getBaseUrl(awsServer);
      this.getVirtues(awsServer);
      this.getApps(awsServer);
    });
  }

  getBaseUrl(url: string) {
    this.baseUrl = url;
    // console.log('getBaseUrl() => ' + this.baseUrl);
  }

  getVirtues(baseUrl: string) {
    this.virtuesService.getVirtues(baseUrl)
      .subscribe(virtues => {
        this.virtues = virtues;
      });
  }

  getApps(baseUrl: string) {
    this.appService.getAppsList(baseUrl).subscribe(data => this.appsList = data);
  }

  getAppName(id: string) {
    let app = this.appsList.filter(data => data.id === id);
    return app[0].name;
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

  cbVirtueList(event, id: string) {
    if (event === true) {
      this.selVirtues.push(id);
      console.log('Added ' + id);
    } else {
      this.removeVm(id);
      console.log('removed ' + id);
    }
  }

  removeVm(id: string) {
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
    console.log('Selected Virtues: ');
    console.log(this.selVirtues);
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
