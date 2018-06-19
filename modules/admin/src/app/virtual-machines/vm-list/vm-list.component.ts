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

  vms: any[];
  apps: any[];
  filterValue = '*';

  // noListData = false;

  baseUrl: string;
  vm: any;
  // these are the default properties the list sorts by
  sortColumn: string = 'name';
  sortType: string = 'enabled';
  sortValue: any = '*';
  sortBy: string = 'asc';
  vmSortType: string = 'enabled'; // This is the default VM datatype
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
      this.totalVms = vmlist.length;
      this.sortVms(vmlist);
    });
  }

  sortVms(sortDirection: string) {
    if (sortDirection === 'asc') {
      this.vms.sort((leftSide, rightSide): number => {
        if (leftSide['name'] < rightSide['name']) {
          return -1;
        }
        if (leftSide['name'] > rightSide['name']) {
          return 1;
        }
        return 0;
      });
    } else {
      this.vms.sort((leftSide, rightSide): number => {
        if (leftSide['name'] < rightSide['name']) {
          return 1;
        }
        if (leftSide['name'] > rightSide['name']) {
          return -1;
        }
        return 0;
      });
    }
  }

  enabledVmList(sortType: string, enabledValue: any, sortBy) {
    console.log('enabledVirtueList() => ' + enabledValue);
    if (this.sortValue !== enabledValue) {
      this.sortBy = 'asc';
    } else {
      this.sortListBy(sortBy);
    }
    this.sortValue = enabledValue;
    this.sortType = sortType;
  }

  sortVmColumns(sortColumn: string, sortBy: string) {
    if (this.sortColumn === sortColumn) {
      this.sortListBy(sortBy);
    } else {
      if (sortColumn === 'name') {
        this.sortBy = 'asc';
        this.sortColumn = sortColumn;
      } else if (sortColumn === 'date') {
        this.sortColumn = sortColumn;
        this.sortBy = 'desc';
      }
    }
  }

  sortListBy(sortDirection: string) {
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

  getAppName(id: string): void {
    for (let app of this.apps) {
      if (id === app.id) {
        return app.name;
      }
    }
  }

  vmStatus(id: string) {
    this.vmService.toggleVmStatus(this.baseUrl, id).subscribe(data => {
      this.vm = data;
    });
    this.resetRouter();
    this.router.navigate(['/vm']);
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
