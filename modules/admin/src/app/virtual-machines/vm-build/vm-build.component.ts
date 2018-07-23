import { Component, OnDestroy, OnInit } from '@angular/core';
import { HttpEvent, HttpHandler, HttpRequest } from '@angular/common/http';
import { FormControl } from '@angular/forms';
import { Router } from '@angular/router';
import { MatDialog } from '@angular/material';
import { Observable } from 'rxjs/Observable';

import { VmAppsModalComponent } from '../vm-apps-modal/vm-apps-modal.component';

import { ApplicationsService } from '../../shared/services/applications.service';
import { BaseUrlService } from '../../shared/services/baseUrl.service';
import { VirtualMachineService } from '../../shared/services/vm.service';

@Component({
  selector: 'app-vm-build',
  templateUrl: './vm-build.component.html',
  providers: [ ApplicationsService, BaseUrlService, VirtualMachineService ]
})
export class VmBuildComponent implements OnInit, OnDestroy {
  vmForm: FormControl;
  activeClass: string;
  baseUrl: string;
  osValue: string;
  selectedOS: string;
  securityTag: string;
  securityLevel: string;

  appList = [];
  appIDsList = [];
  osList = [
    { 'name': 'Debian', 'os': 'LINUX' },
    { 'name': 'Windows', 'os': 'WINDOWS' }
  ];

//TODO this doesn't appear to be used anywhere
  securityOptions = [
    { 'level': 'default', 'name': 'Default'} ,
    { 'level': 'email', 'name': 'Email' },
    { 'level': 'power',  'name': 'Power User' },
    { 'level': 'admin', 'name': 'Administrator' }
  ];

  constructor(
    private vmService: VirtualMachineService,
    private appsService: ApplicationsService,
    private baseUrlService: BaseUrlService,
    private router: Router,
    public dialog: MatDialog,
  ) {
    this.vmForm = new FormControl();
  }

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    console.log(req);
    return next.handle(req);
  }

  ngOnInit() {
    this.baseUrlService.getBaseUrl().subscribe(res => {
      let awsServer = res[0].aws_server;
        this.setBaseUrl(awsServer);
        this.updateAppList([]);
    });
  }

  ngOnDestroy() {
  }

  setBaseUrl(url: string) {
    this.baseUrl = url;
  }

  updateAppList(newAppList) {
    // loop through the selected VM list
    this.appList = [];
    this.appIDsList = newAppList;
    // console.log('vm's app list @ updateAppList(): ' + this.appIDsList);
    const vmAppIDs = newAppList;
    for (let appID of vmAppIDs) {
      this.appsService.getApp(this.baseUrl, appID).subscribe(
        appData => {
          this.appList.push(appData);
        },
        error => {
          console.log(error.message);
        }
      );
    }
  }

  removeApp(id: string, index: number): void {
    this.appList = this.appList.filter(data => {
      return data.id !== id;
    });
    this.appIDsList.splice(index, 1);
  }


  activateModal(): void {

    let dialogRef = this.dialog.open(VmAppsModalComponent, {
      width: '750px',
      data: {
        selectedApps: this.appIDsList
      }
    });
    console.log('Apps sent to dialog: ' + this.appIDsList);
    dialogRef.updatePosition({ top: '5%', left: '20%' });

    const apps = dialogRef.componentInstance.addApps.subscribe((dialogAppsList) => {
      console.log('Apps from dialog: ' + dialogAppsList);

      this.updateAppList(dialogAppsList);
    });

    dialogRef.afterClosed().subscribe(() => {
      apps.unsubscribe();
    });
  }

  buildVirtualMachine(vmName: string, vmOs: string, vmSecurityTag: string) {
    let body = {
      'name': vmName,
      'os': vmOs,
      'loginUser': 'system',
      'enabled': true,
      'applicationIds': this.appIDsList,
      'securityTag': vmSecurityTag
    };
    console.log(body);
    this.vmService.createVM(this.baseUrl, JSON.stringify(body));
    this.router.navigate(['/virtual-machines']);
  }
}
