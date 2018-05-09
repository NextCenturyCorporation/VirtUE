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
    }, 1000);
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
        console.log('error: ' + error.message);
      });
    this.refreshData();
    this.router.navigate(['/vm']);
  }

  deleteVM(id: string) {
    this.vmService.deleteVM(this.baseUrl, id);
    this.refreshData();
  }

  openDialog(id: string, type: string, category: string, description: string): void {
    let dialogRef = this.dialog.open(DialogsComponent, {
      width: '450px',
      data:  {
          dialogType: type,
          dialogCategory: category,
          dialogId: id,
          dialogDescription: description
        }
    });

    dialogRef.updatePosition({ top: '15%', left: '36%' });

    const dialogResults = dialogRef.componentInstance.dialogEmitter.subscribe((data) => {
      // console.log('Dialog Emitter: ' + data);
      if (type === 'delete') {
        this.deleteVM(data);
      }
    });
  }
}
