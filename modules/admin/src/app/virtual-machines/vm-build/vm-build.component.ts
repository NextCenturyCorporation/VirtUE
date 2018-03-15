import { Component, OnInit } from '@angular/core';
import { FormControl, FormGroup } from '@angular/forms';
import { MatDialog, MatDialogRef } from '@angular/material';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs/Observable';

import { DialogsComponent } from '../../dialogs/dialogs.component';
import { VmAppsModalComponent } from '../vm-apps-modal/vm-apps-modal.component';

import { VirtualMachineService } from '../../shared/services/vm.service';
import { VmAppsService } from '../../shared/services/vm-apps.service';

@Component({
  selector: 'app-vm-build',
  templateUrl: './vm-build.component.html',
  styleUrls: ['./vm-build.component.css'],
  providers: [VirtualMachineService, VmAppsService]
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

  constructor(
    private vmService: VirtualMachineService,
    private appsService: VmAppsService,
    public dialog: MatDialog,
  ) { }

  ngOnInit() {
    this.getAppList();
  }

  getAppList() {
    // loop through the selected VM list
    const selectedApps = this.pageAppList;
    console.log('page Apps list @ getAppList(): ' + this.pageAppList);
    this.appsService.getAppsList()
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
          // this.getUpdatedVmList();
        }
      });
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

      this.getAppList();
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
