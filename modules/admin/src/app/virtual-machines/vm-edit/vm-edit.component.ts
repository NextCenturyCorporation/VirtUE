import { Component, Input, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Location } from '@angular/common';
import { MatDialog, MatDialogRef } from '@angular/material';

import { VirtualMachine } from '../../shared/models/vm.model';
import { Application } from '../../shared/models/application.model';
import { VmAppsService } from '../../shared/services/vm-apps.service';
import { VirtualMachineService } from '../../shared/services/vm.service';

import { DialogsComponent } from '../../dialogs/dialogs.component';
import { VmAppsModalComponent } from '../vm-apps-modal/vm-apps-modal.component';

@Component({
  selector: 'app-vm-edit',
  templateUrl: './vm-edit.component.html',
  styleUrls: ['./vm-edit.component.css'],
  providers: [ VirtualMachineService, VmAppsService ]
})
export class VmEditComponent implements OnInit {

  @Input() vm: VirtualMachine;

  vmId: { id: string };
  vmData = [];
  vmApps = [];
  appList = [];

  selectedApps = [];
  selAppList = [];
  pageAppList = [];

  appsInput = '';
  osValue: string;
  osInfo: string;
  osList = [
    { 'id': 10, 'os_name': 'LINUX', 'info': 'https://packages.debian.org/stable/' },
    { 'id': 11, 'os_name': 'Windows', 'info': 'https://www.microsoft.com/en-us/windows/' }
  ];
  os: string;
  selected: string;

  constructor(
    private router: ActivatedRoute,
    private vmService: VirtualMachineService,
    private appsService: VmAppsService,
    private location: Location,
    public dialog: MatDialog
  ) { }

  ngOnInit() {
    this.vmId = {
      id: this.router.snapshot.params['id']
    };
    this.getThisVm(this.vmId.id);
  }

  getThisVm(id: string) {
    this.vmService.getVM(id).subscribe(
      data => {
        this.vmData = data;
        this.selected = data.os;
        this.pageAppList = data.applicationIds;
        this.getAppList(data.applicationIds);
        console.log(this.vmData);
      }
    );
  }

  getAppList(vmApps) {
    // loop through the selected VM list
    const selectedApps = this.pageAppList;
    console.log('getAppList: ' + this.pageAppList);
    this.appsService.getAppsList()
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
          this.getUpdatedAppList();
        }
      });
  }

  getUpdatedAppList() {
    this.appList = [];
    this.appsService.getAppsList()
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

      this.getAppList(this.pageAppList);
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
