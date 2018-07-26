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
  templateUrl: './vm-list.component.html',
  styleUrls: ['./vm-list.component.css']
})
export class VmListComponent implements OnInit {

  vms = [];
  apps = [];
  // noListData = false;

  baseUrl: string;
  vm: any;
  // these are the default properties the list sorts by
  sortColumn: string = 'name';
  sortType: string = 'enabled';
  sortValue: any = '*';
  sortBy: string = 'asc';
  totalVms: number = 0;

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
      this.getAppsList(awsServer);
      this.getVmList(awsServer);
    });
    this.resetRouter();
  }

  getBaseUrl(url: string) {
    this.baseUrl = url;
  }

  resetRouter() {
    setTimeout(() => {
      this.router.navigated = false;
      this.getVmList(this.baseUrl);
    }, 1000);
  }

  getVmList(baseUrl: string) {
    this.vmService.getVmList(baseUrl).subscribe(vmlist => {
      this.vms = vmlist;
      // console.log("*****");
      // console.log(this.vms[0]);
      for (var vm of this.vms) {
        vm.status = vm.enabled ? 'enabled' : 'disabled';
      }
      // console.log(this.vms[0]);
      this.totalVms = vmlist.length;
      // this.sortVms(vmlist); //what? This should be expecting a sortDirection, no?
      // this.sortVms('dsc');
      // oh and changing or even commenting this line out entirely doesn't seem to
      // affect the order, initial or otherwise, of the shown data.
      // the called function is commented out below as well.
    });
  }

  // Appears to be unnecessary. See comment in getVmList()
  // sortVms(sortDirection: string) {
  //   console.log("sortVms");
  //   if (sortDirection === 'asc') {
  //     this.vms.sort((leftSide, rightSide): number => {
  //       console.log("asc");
  //       // console.log(JSON.stringify(leftSide));
  //       if (leftSide['name'] < rightSide['name']) {
  //         return -1;
  //       }
  //       if (leftSide['name'] > rightSide['name']) {
  //         return 1;
  //       }
  //       return 0;
  //     });
  //   } else {
  //     this.vms.sort((leftSide, rightSide): number => {
  //       console.log("dsc");
  //       // console.log(JSON.stringify(leftSide));
  //       if (leftSide['name'] < rightSide['name']) {
  //         return 1;
  //       }
  //       if (leftSide['name'] > rightSide['name']) {
  //         return -1;
  //       }
  //       return 0;
  //     });
  //   }
  // }

  enabledVmList(sortType: string, enabledValue: any, sortBy) {
    console.log('enabledVmList() => ' + enabledValue);
    if (this.sortValue !== enabledValue) {
      this.sortBy = 'asc';
    } else {
      this.reverseSortDirection(sortBy);
    }
    this.sortValue = enabledValue;
    this.sortType = sortType;
  }

  setColumnSort(sortColumn: string, sortBy: string) {
    console.log("sortVmColumns");
    console.log(this.vms[0]["name"]);
    if (this.sortColumn === sortColumn) {
      this.reverseSortDirection(sortBy);
    } else {
      switch( sortColumn ) {
        case 'name' :
        case 'os':
        case 'lastEditor':
        case 'securityTag':
          this.sortBy = 'asc';
	  break;
        case 'date':
          this.sortBy = 'desc';
	  break;
      }
      this.sortColumn = sortColumn;
    }
  }

  reverseSortDirection(sortDirection: string) {
    if (sortDirection === 'asc') {
      this.sortBy = 'desc';
    } else {
      this.sortBy = 'asc';
    }
  }

  getAppsList(baseUrl: string) {
    this.appsService.getAppsList(baseUrl)
    .subscribe(appList => {
      this.apps = appList;
    });
  }

  getAppName(id: string) {
    if (id) {
      let selApp = this.apps.filter(app => id === app.id)
        .map(appName => {
          return appName.name;
        });
      return selApp;
    }
  }

  vmStatus(id: string) {
    this.vmService.toggleVmStatus(this.baseUrl, id).subscribe(data => {
      this.vm = data;
    });
    this.resetRouter();
    this.router.navigate(['/virtual-machines']);
  }

  deleteVM(id: string) {
    this.vmService.deleteVM(this.baseUrl, id);
    this.resetRouter();
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
