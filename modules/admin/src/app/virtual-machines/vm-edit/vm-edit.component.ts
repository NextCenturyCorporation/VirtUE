import { Component, Input, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Location } from '@angular/common';

import { VirtualMachine } from '../../shared/models/vm.model';
import { Application } from '../../shared/models/application.model';
import { VirtualMachineService } from '../../shared/services/vm.service';

import { MatDialog, MatDialogRef } from '@angular/material';
import { DialogsComponent } from '../../dialogs/dialogs.component';

@Component({
  selector: 'app-vm-edit',
  templateUrl: './vm-edit.component.html',
  styleUrls: ['./vm-edit.component.css'],
  providers: [ VirtualMachineService ]
})
export class VmEditComponent implements OnInit {

  @Input() vm: VirtualMachine;

  vmId: { id: string };
  vmData = [];
  appList = [];
  appsInput = '';
  osValue: string;
  osInfo: string;
  osList = [
    { 'id': 10, 'os': 'LINUX', 'info': 'https://packages.debian.org/stable/' },
    { 'id': 11, 'os': 'Windows', 'info': 'https://www.microsoft.com/en-us/windows/' }
  ];
  os: string;
  selectedValue: string;

  constructor(
    private router: ActivatedRoute,
    private vmService: VirtualMachineService,
    private location: Location,
    public dialog: MatDialog
  ) { }

  ngOnInit() {
    this.vmId = {
      id: this.router.snapshot.params['id']
    };
    this.getThisVm();
  }
  getThisVm() {
    const id = this.vmId.id;

    this.vmService.getVM(id).subscribe(
      data => {
        this.vmData = data;
        this.selectedValue = data.os;
        this.appList = data.applications;
        this.getAppList(data.applications);
      }
    );

  }
  getOsInfo(os) {
    const osList = this.osList;
    for (let i in osList) {
      if (os === this.osList[i].os) {
        return this.osList[i].info;
      }
    }
  }
  getAppList(apps) {
    for (let i of apps) {
      if (apps[i].name) {
        console.log(apps[i].name);
        this.appsInput += apps[i].name + '\n';
      }
      this.appsInput.trim();
    }
  }

}
