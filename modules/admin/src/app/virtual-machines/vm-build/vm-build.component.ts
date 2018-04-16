import { Component, OnInit } from '@angular/core';
import { FormControl, FormGroup } from '@angular/forms';
import { MatDialog, MatDialogRef } from '@angular/material';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs/Observable';

import { DialogsComponent } from '../../dialogs/dialogs.component';
import { VmAppsModalComponent } from '../vm-apps-modal/vm-apps-modal.component';

import { ApplicationsService } from '../../shared/services/applications.service';
import { BaseUrlService } from '../../shared/services/baseUrl.service';
import { VirtualMachineService } from '../../shared/services/vm.service';

@Component({
  selector: 'app-vm-build',
  templateUrl: './vm-build.component.html',
  styleUrls: ['./vm-build.component.css'],
  providers: [ ApplicationsService, BaseUrlService, VirtualMachineService ]
})
export class VmBuildComponent implements OnInit {
  osValue: string;
  osInfo: string;
  osList = [
    { 'id': 10, 'os_name': 'CentOS', 'os_info': 'http://mirror.centos.org/centos/7/os/x86_64/Packages/' },
    { 'id': 11, 'os_name': 'Debian', 'os_info': 'https://packages.debian.org/stable/' },
    { 'id': 12, 'os_name': 'Fedora', 'os_info': 'https://apps.fedoraproject.org/' },
    { 'id': 13, 'os_name': 'Red Hat Linux', 'os_info': 'https://access.redhat.com/downloads' },
    { 'id': 14, 'os_name': 'Windows', 'os_info': 'https://www.microsoft.com/en-us/windows/' }
  ];

  activeClass: string;

  appList = [];
  selAppList = [];
  pageAppList = [];
  baseUrl: string;

  constructor(
    private vmService: VirtualMachineService,
    private appsService: ApplicationsService,
    private baseUrlService: BaseUrlService,
    public dialog: MatDialog,
  ) { }

  ngOnInit() {
    this.baseUrlService.getBaseUrl().subscribe(res => {
      let awsServer = res[0].aws_server;
        this.getAppList(awsServer);
    });
  }

  getAppList(baseUrl: string) {
    this.baseUrl = baseUrl;
    // loop through the selected VM list
    const selectedApps = this.pageAppList;
    console.log('page Apps list @ getAppList(): ' + this.pageAppList);
    this.appsService.getAppsList(baseUrl)
      .subscribe(apps => {
        if (this.appList.length < 1) {
          for (let sel of selectedApps) {
            for (let app of apps) {
              if (sel === app.id) {
                this.appList.push(app);
                break;
              }
            }
          }
        } else {
          this.getUpdatedAppList(baseUrl);
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
    console.log('Apps sent to dialog: ' + this.pageAppList);
    dialogRef.updatePosition({ top: '5%', left: '20%' });

    const apps = dialogRef.componentInstance.addApps.subscribe((data) => {
      this.selAppList = data;
      console.log('Apps from dialog: ' + this.selAppList);
      if (this.pageAppList.length > 0) {
        this.pageAppList = [];
      }
      this.pageAppList = this.selAppList;

      this.getAppList(this.baseUrl);
    });

    dialogRef.afterClosed().subscribe(() => {
      apps.unsubscribe();
    });
  }

  onBuildVM(name, os, packages) {
    // const buildDate: Date = new Date();
    // const pkgs = packages.replace(/\n/g,'|');
    // const vmFields='{'vm_name':''+name+''},{'vm_os':''+os+''},{'vm_packages':''+pkgs+''},
    // {'vm_timestamp':''+buildDate+''},{'vm_status':'disabled'}';
    // console.log('new values: '+name+','+os+','+pkgs+','+buildDate);
    // this.jsondataService.addNewData('vms',vmFields);
  }

}
