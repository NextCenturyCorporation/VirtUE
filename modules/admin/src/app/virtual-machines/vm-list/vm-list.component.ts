import { Component, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material';
import { Router } from '@angular/router';

import { ApplicationsService } from '../../shared/services/applications.service';
import { BaseUrlService } from '../../shared/services/baseUrl.service';
import { VirtualMachineService } from '../../shared/services/vm.service';
import { DialogsComponent } from '../../dialogs/dialogs.component';

@Component({
  selector: 'app-vm-list',
  providers: [ BaseUrlService, VirtualMachineService, ApplicationsService ],
  templateUrl: './vm-list.component.html'
})
export class VmListComponent implements OnInit {

  vms = [];
  apps = [];
  filterValue = '*';

  // noListData = false;

  baseUrl: string;
  vm: any;
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
    this.filterValue = isEnabled;
    this.totalVms = this.vms.length;
  }

  // updateVmStatus(id: string, isEnabled: boolean) {
    // this.vmService.toggleVmStatus(this.baseUrl, id).subscribe(data => {
    //   this.vm = data;
    // });
  //   this.refreshData();
  //   this.router.navigate(['/vm']);
  // }

  updateVmStatus(id: string, isEnabled: boolean): void {
    let vmStatus: boolean;
    if (isEnabled) {
      vmStatus = false;
    } else {
      vmStatus = true;
    }
    console.log('updating status for vm #' + id);
    let body = {
      'enabled': vmStatus
    };
    console.log(body);
    this.vmService.updateVmStatus(this.baseUrl, id, JSON.stringify(body)).subscribe(success => {
      console.log('success');
    }, err => {
      console.log(err);
      }
    );
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
