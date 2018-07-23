import { Component, Input, OnInit } from '@angular/core';
import { FormControl } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { MatDialog } from '@angular/material';

import { VirtualMachine } from '../../shared/models/vm.model';
import { BaseUrlService } from '../../shared/services/baseUrl.service';
import { ApplicationsService } from '../../shared/services/applications.service';
import { VirtualMachineService } from '../../shared/services/vm.service';

import { VmAppsModalComponent } from '../vm-apps-modal/vm-apps-modal.component';

@Component({
  selector: 'app-vm-edit',
  templateUrl: './vm-edit.component.html',
  providers: [ ApplicationsService, BaseUrlService, VirtualMachineService ]
})
export class VmEditComponent implements OnInit {

  @Input() vm: VirtualMachine;

  vmForm: FormControl;
  vmId: string;
  baseUrl: string;
  os: string;
  osValue: string;
  selectedOS: string;
  securityTag: string;
  securityLevel: string;
  vmEnabled: boolean;

  vmData = [];
  appList = [];
  appIDsList = [];
  osList = [
    { 'name': 'Debian', 'os': 'LINUX' },
    { 'name': 'Windows', 'os': 'WINDOWS' }
  ];
  securityOptions = [
    { 'level': 'default', 'name': 'Default' },
    { 'level': 'email', 'name': 'Email' },
    { 'level': 'power', 'name': 'Power User' },
    { 'level': 'admin', 'name': 'Administrator' }
  ];

  constructor(
    private activatedRoute: ActivatedRoute,
    private appsService: ApplicationsService,
    private baseUrlService: BaseUrlService,
    private router: Router,
    private vmService: VirtualMachineService,
    public dialog: MatDialog
  ) { }

  ngOnInit() {
    this.vmId = this.activatedRoute.snapshot.params['id'];
    this.baseUrlService.getBaseUrl().subscribe(res => {
      let awsServer = res[0].aws_server;
      this.setBaseUrl(awsServer);
      this.getThisVm(this.vmId);
    });
  }

  resetRouter() {
    setTimeout(() => {
      this.router.navigated = false;
      this.getThisVm(this.vmId);
    }, 1000);
  }

  setBaseUrl(url: string) {
    this.baseUrl = url;
  }

  getThisVm(id: string) {
    this.vmService.getVM(this.baseUrl, id).subscribe(
      data => {
        this.vmData = data;
        this.selectedOS = data.os;
        this.updateAppList(data.applicationIds);
        this.securityLevel = data.securityTag;
        this.vmEnabled = data.enabled;
      }
    );
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

  toggleVmStatus(isEnabled: boolean) {
    this.vmEnabled = !isEnabled;
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
    dialogRef.updatePosition({ top: '5%', left: '20%' });

    const apps = dialogRef.componentInstance.addApps.subscribe((dialogAppsList) => {
      this.updateAppList(dialogAppsList);
    });

    dialogRef.afterClosed().subscribe(() => {
      apps.unsubscribe();
    });
  }

  buildVirtualMachine(id: string, vmName: string, vmOs: string, vmSecurityTag: string) {
    let body = {
      'name': vmName,
      'os': vmOs,
      'loginUser': 'system',
      'enabled': this.vmEnabled,
      'applicationIds': this.appIDsList,
      'securityTag': vmSecurityTag
    };
    this.vmService.updateVM(this.baseUrl, id, JSON.stringify(body));
    this.resetRouter();
    this.router.navigate(['/virtual-machines']);
  }

  cancel() {
    this.router.navigate(['/virtual-machines']);
  }

}
