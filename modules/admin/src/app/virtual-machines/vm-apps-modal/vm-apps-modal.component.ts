import { Component, EventEmitter, Inject, Input, OnInit } from '@angular/core';
import { FormGroup } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';

import { ApplicationsService } from '../../shared/services/applications.service';
import { BaseUrlService } from '../../shared/services/baseUrl.service';
import { Application } from '../../shared/models/application.model';

import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material';

@Component({
  selector: 'app-vm-modal',
  templateUrl: './vm-apps-modal.component.html',
  styleUrls: ['./vm-apps-modal.component.css'],
  providers: [ ApplicationsService, BaseUrlService ]
})
export class VmAppsModalComponent implements OnInit {
  @Input() appInput: Application;

  form: FormGroup;
  virtueId: string;

  checked = false;

  addApps = new EventEmitter();
  appList = [];
  selAppList = [];
  pageAppList = [];

  constructor(
    private route: ActivatedRoute,
    private appsService: ApplicationsService,
    private baseUrlService: BaseUrlService,
    private dialogRef: MatDialogRef<VmAppsModalComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any
  ) {
      this.pageAppList = data['selectedApps'];
    }

  ngOnInit() {
    this.baseUrlService.getBaseUrl().subscribe(res => {
      let awsServer = res[0].aws_server;
      this.getAppsList(awsServer);
    });

    if (this.pageAppList.length > 0) {
      this.selAppList = this.pageAppList;
    }
  }

  getAppsList(baseUrl: string) {
    this.appsService.getAppsList(baseUrl)
      .subscribe(apps => {
        this.appList = apps;
      });
  }

  selectApp(id: string) {
    if (this.pageAppList.length > 0) {
      for (let sel of this.pageAppList) {
        if (sel === id) {
          return true;
        }
      }
    } else {
      return false;
    }
  }

  selectAll(event) {
    if (event) {
      this.checked = true;
      for (let vm of this.appList) {
        this.selAppList.push(vm.id);
      }
    } else {
      this.checked = false;
      this.clearAppsList();
    }
  }

  cbAppList(event, id: string, index: number) {
    if (event === true) {
      this.selAppList.push(id);
    } else {
      this.removeApp(id, index);
    }
  }

  removeApp(id: string, index: number) {
    this.selAppList.splice(this.selAppList.indexOf(id), 1);
  }

  clearAppsList() {
    this.selAppList = [];
    this.pageAppList = [];
  }

  onAddApps(): void {
    // if (this.pageAppList.length > 0) {
    //   this.selAppList = this.pageAppList;
    // }
    this.addApps.emit(this.selAppList);
    this.clearAppsList();
    this.dialogRef.close();
  }

  cancelModal() {
    this.clearAppsList();
    this.dialogRef.close();
  }

}
