import { Component, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material';
import { Router } from '@angular/router';

import { ApplicationsService } from '../../shared/services/applications.service';
import { BaseUrlService } from '../../shared/services/baseUrl.service';
import { VirtualMachineService } from '../../shared/services/vm.service';
import { DialogsComponent } from '../../dialogs/dialogs.component';

import { Application } from '../../shared/models/application.model';
import { VirtualMachine } from '../../shared/models/vm.model';

@Component({
  selector: 'app-vm-list',
  providers: [ BaseUrlService, VirtualMachineService, ApplicationsService ],
  templateUrl: './vm-list.component.html',
  styleUrls: ['./vm-list.component.css']
})
export class VmListComponent implements OnInit {

  vms: VirtualMachine[];
  allApps: Application[];
  // noListData = false;

  baseUrl: string;
  // vm: any;
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
    this.vms = new Array<VirtualMachine>()
    // override the route reuse strategy
    this.router.routeReuseStrategy.shouldReuseRoute = function() {
      return false;
    };
  }

  ngOnInit() {
    this.baseUrlService.getBaseUrl().subscribe(res => {
      let awsServer = res[0].aws_server;
      this.getBaseUrl(awsServer);
      this.getVms();
      this.getAppsList();
    });
    this.resetRouter();
  }

  getBaseUrl(url: string) {
    this.baseUrl = url;
  }

  resetRouter() {
    setTimeout(() => {
      this.router.navigated = false;
    }, 1000);
  }

  refreshData() {
    setTimeout(() => {
      this.getVms();
    }, 300);
  }

  getVms() {
    this.vmService.getVmList(this.baseUrl).subscribe(vmList => {
      this.vms = vmList; //TODO fix backend to give new Virtue objects
      this.totalVms = vmList.length;
    });
  }


  enabledVmList(sortType: string, enabledValue: any, sortBy) {
    console.log('enabledVmList() => ' + enabledValue);
    if (this.sortValue !== enabledValue) {
      this.sortBy = 'asc';
    } else {
      this.sortListBy(sortBy);
    }
    this.sortValue = enabledValue;
    this.sortType = sortType;
  }

  sortVmColumns(sortColumn: string, sortBy: string) {
    console.log("sortVmColumns");
    if (this.sortColumn === sortColumn) {
      this.sortListBy(sortBy);
    } else {
      if (sortColumn === 'name') {
        this.sortBy = 'asc';
        this.sortColumn = sortColumn;
      } else if (sortColumn === 'os') {
        this.sortBy = 'asc';
        this.sortColumn = sortColumn;
      } else if (sortColumn === 'lastEditor') {
        this.sortBy = 'asc';
        this.sortColumn = sortColumn;
      } else if (sortColumn === 'securityTag') {
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

  getAppsList() {
    this.appsService.getAppsList(this.baseUrl)
    .subscribe(appList => {
      this.allApps = appList;
    });
  }

  getAppName(id: string) {
    if (id) {
      let selApp = this.allApps.filter(app => id === app.id)
        .map(appName => {
          return appName.name;
        });
      return selApp;
    }
  }

  toggleVmStatus(id: string) {
    //for some reason, I need to subscribe in order for the toggle
    //to work, even if I don't do anything with the stuff I'm subscribed to
    // That data certainly isn't supposed to go there.
    this.vmService.toggleVmStatus(this.baseUrl, id).subscribe();//data => {
    //    this.vm = data;
    // });
    this.refreshData();

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
