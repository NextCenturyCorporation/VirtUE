import { Component, Input, OnInit} from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { FormControl } from '@angular/forms';
import { MatDialog } from '@angular/material';

import { VmAppsModalComponent } from '../vm-apps-modal/vm-apps-modal.component';

import { VirtualMachine } from '../../shared/models/vm.model';
import { ApplicationsService } from '../../shared/services/applications.service';
import { BaseUrlService } from '../../shared/services/baseUrl.service';
import { VirtualMachineService } from '../../shared/services/vm.service';

@Component({
  selector: 'app-vm-duplicate',
  templateUrl: './vm-duplicate.component.html',
  providers: [ ApplicationsService, BaseUrlService, VirtualMachineService ]
})
export class VmDuplicateComponent implements OnInit {
  @Input() vm: VirtualMachine;

  vmForm: FormControl;
  vmId: string;
  baseUrl: string;
  os: string;
  osValue: string;
  selectedOS: string;
  securityTag: string;
  securityLevel: string;

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
      this.getVmData(this.vmId);
    });
  }

  setBaseUrl(url: string) {
    this.baseUrl = url;
  }

  getVmData(id: string) {
    this.vmService.getVM(this.baseUrl, id).subscribe(
      data => {
        this.vmData = data;
        this.vmData['name'] = 'Copy of ' + this.vmData['name'];
        this.selectedOS = data.os;
        this.updateAppList(data.applicationIds);
        this.securityLevel = data.securityTag;
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

  duplicateVirtualMachine(vmName: string, vmOs: string, vmSecurityTag: string) {
    let body = {
      'name': vmName,
      'os': vmOs,
      'loginUser': 'system',
      'enabled': true,
      'applicationIds': this.appIDsList,
      'securityTag': vmSecurityTag
    };
    this.vmService.createVM(this.baseUrl, JSON.stringify(body));
    this.router.navigate(['/virtual-machines']);
  }

  cancel() {
    this.router.navigate(['/virtual-machines']);
  }

}
