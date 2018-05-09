import { Component, Input, OnInit } from '@angular/core';
import { Location } from '@angular/common';
import { FormControl, FormGroup } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { MatDialog, MatDialogRef } from '@angular/material';

import { VirtualMachine } from '../../shared/models/vm.model';
import { Application } from '../../shared/models/application.model';

import { BaseUrlService } from '../../shared/services/baseUrl.service';
import { ApplicationsService } from '../../shared/services/applications.service';
import { VirtualMachineService } from '../../shared/services/vm.service';

import { DialogsComponent } from '../../dialogs/dialogs.component';
import { VmAppsModalComponent } from '../vm-apps-modal/vm-apps-modal.component';

@Component({
  selector: 'app-vm-edit',
  templateUrl: './vm-edit.component.html',
  styleUrls: ['./vm-edit.component.css'],
  providers: [ ApplicationsService, BaseUrlService, VirtualMachineService ]
})
export class VmEditComponent implements OnInit {

  @Input() vm: VirtualMachine;

  vmForm: FormControl;
  vmId: { id: string };
  appsInput: string;
  baseUrl: string;
  os: string;
  osValue: string;
  selectedOS: string;
  securityTag: string;
  securityLevel: string;

  vmData = [];
  vmApps = [];
  appList = [];
  selectedApps = [];
  selAppList = [];
  pageAppList = [];
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
    private location: Location,
    public dialog: MatDialog
  ) { }

  ngOnInit() {
    this.vmId = {
      id: this.activatedRoute.snapshot.params['id']
    };
    this.baseUrlService.getBaseUrl().subscribe(res => {
      let awsServer = res[0].aws_server;
      this.getBaseUrl(awsServer);
      this.getThisVm(awsServer, this.vmId.id);
    });
  }

  getBaseUrl(url: string) {
    this.baseUrl = url;
  }

  getThisVm(baseUrl: string, id: string) {
    this.baseUrl = baseUrl;
    this.vmService.getVM(baseUrl, id).subscribe(
      data => {
        this.vmData = data;
        this.selectedOS = data.os;
        this.pageAppList = data.applicationIds;
        this.getAppList(data.applicationIds);
        this.securityLevel = data.securityTag;
      }
    );
  }

  getAppList(vmApps) {
    const selectedApps = this.pageAppList;
    this.appsService.getAppsList(this.baseUrl)
      .subscribe(apps => {
        if (selectedApps.length < 1) {
          for (let sel of selectedApps) {
            for (let app of apps) {
              if (sel === app.id) {
                this.appList.push(app);
                break;
              }
            }
          }
        } else {
          this.getUpdatedAppList(this.baseUrl);
        }
      });
  }

  getUpdatedAppList(baseUrl: string) {
    this.appList = [];
    this.appsService.getAppsList(baseUrl)
      .subscribe(apps => {
        for (let sel of this.pageAppList) {
          for (let app of apps) {
            if (sel === app.id) {
              this.appList.push(app);
              break;
            }
          }
        }
      });
  }

  removeApp(id: string, index: number): void {
    this.appList = this.appList.filter(data => {
      return data.id !== id;
    });
    this.pageAppList.splice(index, 1);
  }


  activateModal(): void {

    let dialogRef = this.dialog.open(VmAppsModalComponent, {
      width: '750px',
      data: {
        selectedApps: this.pageAppList
      }
    });
    dialogRef.updatePosition({ top: '5%', left: '20%' });

    const apps = dialogRef.componentInstance.addApps.subscribe((data) => {
      this.selAppList = data;
      if (this.pageAppList.length > 0) {
        this.pageAppList = [];
      }
      this.pageAppList = this.selAppList;

      this.getAppList(this.pageAppList);
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
      'enabled': true,
      'applicationIds': this.pageAppList,
      'securityTag': vmSecurityTag
    };
    this.vmService.updateVM(this.baseUrl, id, JSON.stringify(body));
    this.router.navigate(['/vm']);
  }

  cancel() {
    this.router.navigate(['/vm']);
  }

}
