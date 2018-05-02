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
  vmStatus: boolean;

  constructor(
    private vmService: VirtualMachineService,
    private appsService: ApplicationsService,
    private baseUrlService: BaseUrlService,
    public dialog: MatDialog,
    public router: Router
  ) {}

  ngOnInit() {
    this.baseUrlService.getBaseUrl().subscribe(res => {
      let url = res[0].aws_server;
      this.getBaseUrl(url);
      this.getVmList(url);
      this.getAppsList(url);
    });
  }

  resetRouter() {
    setTimeout(() => {
      this.router.navigated = false;
    }, 500);
  }

  getBaseUrl(url: string) {
    this.baseUrl = url;
  }

  getVmList(baseUrl: string) {
    this.vmService.getVmList(baseUrl).subscribe(vmlist => {
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

  listFilter(isEnabled: any) {
    // console.log('filterValue = ' + status);
    this.filterValue = isEnabled;
    this.totalVms = this.vms.length;
  }

  updateVmStatus(id: string, isEnabled: boolean): void {
    if (isEnabled) {
      this.vmStatus = false;
    } else {
      this.vmStatus = true;
    }
    console.log('updating status for vm #' + id);
    this.vmService.updateStatus(this.baseUrl, id, this.vmStatus).subscribe( data => {
      return true;
      },
      error => {
        console.log("error: " + error.message);
      });
    this.resetRouter();
    this.router.navigate(['/vm']);
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
