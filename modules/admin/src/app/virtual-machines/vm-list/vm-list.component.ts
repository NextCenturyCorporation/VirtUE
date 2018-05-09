import { Component, OnInit } from '@angular/core';
import { MatDialog, MatDialogRef } from '@angular/material';
import { Routes, RouterModule, Router } from '@angular/router';

import { ApplicationsService } from '../../shared/services/applications.service';
import { BaseUrlService } from '../../shared/services/baseUrl.service';
import { VirtualMachineService } from '../../shared/services/vm.service';
import { DialogsComponent } from '../../dialogs/dialogs.component';

import { ActiveClassDirective } from '../../shared/directives/active-class.directive';

@Component({
  selector: 'app-vm-list',
  providers: [ BaseUrlService, VirtualMachineService, ApplicationsService ],
  templateUrl: './vm-list.component.html',
  styleUrls: ['./vm-list.component.css']
})
export class VmListComponent implements OnInit {

  vms = [];
  apps = [];
  filterValue = '*';
  noListData = false;
  baseUrl: string;
  vmlist: string;
  totalVms: number;

  constructor(
    private vmService: VirtualMachineService,
    private appsService: ApplicationsService,
    private baseUrlService: BaseUrlService,
    private router: Router,
    public dialog: MatDialog
  ) {
    // override the route reuse strategy
    this.router.routeReuseStrategy.shouldReuseRoute = function() {
      return false;
    };
  }

  ngOnInit() {
    this.baseUrlService.getBaseUrl().subscribe(res => {
      let awsServer = res[0].aws_server;
      this.getBaseUrl(awsServer);
      this.getVmList(awsServer);
      this.getAppsList(awsServer);
    });
    this.refreshData();
  }

  getBaseUrl(url: string) {
    this.baseUrl = url;
    console.log('URL: ' + url);
  }

  refreshData() {
    setTimeout(() => {
      this.router.navigated = false;
      this.getVmList(this.baseUrl);
    }, 2000);
  }

  getVmList(baseUrl: string) {
    this.vmService.getVmList(baseUrl)
      .subscribe(vmlist => {
        this.vms = vmlist;
        this.totalVms = vmlist.length;
      });
  }

  getAppsList(baseUrl: string) {
    this.appsService.getAppsList(baseUrl)
    .subscribe(appList => {
      this.apps = appList;
    });
  }

  getAppName(id: string): void {
    for (let app of this.apps) {
      if (id === app.id) {
        return app.name;
      }
    }
  }

  listFilter(status: any) {
    // console.log('filterValue = ' + status);
    this.filterValue = status;
    this.totalVms = this.vms.length;
  }

  updateStatus(id: string): void {
    const vm = this.vms.filter(data => data['id'] === id);
    // vm.map((_, i) => {
    //   vm[i].enabled ? vm[i].enabled = false : vm[i].enabled = true;
    //   console.log(vm);
    // });
    // this.appsService.update(id, app);
  }

  openDialog(id, type, action, text): void {
    let dialogRef = this.dialog.open(DialogsComponent, {
      width: '450px',
      data:  {
          dialogText: text,
          dialogType: type
        }
    });

    dialogRef.updatePosition({ top: '15%', left: '36%' });

    dialogRef.afterClosed().subscribe(result => {
      console.log('The dialog was closed');
    });
  }
  // addVM(name: string, status: string) {
  //   this.vms.push({name: name, status: status});
  // }
}
