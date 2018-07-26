import { Component, OnInit } from '@angular/core';
import { HttpEvent, HttpHandler, HttpRequest } from '@angular/common/http';
import { MatDialog } from '@angular/material';
import { Router } from '@angular/router';
import { Observable } from 'rxjs/Observable';

import { DialogsComponent } from '../../dialogs/dialogs.component';

import { BaseUrlService } from '../../shared/services/baseUrl.service';
import { VirtuesService } from '../../shared/services/virtues.service';
import { VirtualMachineService } from '../../shared/services/vm.service';
import { ApplicationsService } from '../../shared/services/applications.service';

@Component({
  selector: 'app-virtue-list',
  providers: [ ApplicationsService, BaseUrlService, VirtuesService, VirtualMachineService ],
  templateUrl: './virtue-list.component.html',
  styleUrls: ['./virtue-list.component.css']
})

export class VirtueListComponent implements OnInit {
  virtue: any;
  title = 'Virtues';
  virtues = [];
  vmList = [];
  appsList = [];
  baseUrl: string;
  // these are the default properties the list sorts by
  sortColumn: string = 'name';
  sortType: string = 'enabled';
  sortValue: any = '*';
  sortBy: string = 'asc';
  totalVirtues: number = 0;
  // virtueTotal: number; I hope this wasn't commented out for a good reason
  os: Observable<Array<VirtuesService>>;

  constructor(
    private router: Router,
    private appsService: ApplicationsService,
    private baseUrlService: BaseUrlService,
    private virtuesService: VirtuesService,
    private vmService: VirtualMachineService,
    public dialog: MatDialog
  ) {
    this.router.routeReuseStrategy.shouldReuseRoute = function() {
      return false;
    };
  }

  ngOnInit() {
    this.baseUrlService.getBaseUrl().subscribe( res => {
      let awsServer = res[0].aws_server;
      this.getBaseUrl(awsServer);
      this.getVirtues(awsServer);
      this.getApplications(awsServer);
      this.getVmList(awsServer);
    });
    this.refreshData();
  }

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    return next.handle(req);
  }

  resetRouter() {
    setTimeout(() => {
      this.router.navigated = false;
    }, 1000);
  }

  getBaseUrl(url: string) {
    this.baseUrl = url;
  }

  refreshData() {
    setTimeout(() => {
      this.router.navigated = false;
      this.getVirtues(this.baseUrl);
    }, 1000);
  }

  getVirtues(baseUrl: string) {
    this.virtuesService.getVirtues(baseUrl).subscribe( virtueList => {
      this.virtues = virtueList;
      this.totalVirtues = virtueList.length;
    });
  }

  enabledVirtueList(sortType: string, enabledValue: any, sortBy) {
    console.log('enabledVirtueList() => ' + enabledValue);
    if (this.sortValue !== enabledValue) {
      this.sortBy = 'asc';
    } else {
      this.reverseSortDirection(sortBy);
    }
    this.sortValue = enabledValue;
    this.sortType = sortType;
  }

  setColumnSort(sortColumn: string, sortBy: string) {
    if (this.sortColumn === sortColumn) {
      this.reverseSortDirection(sortBy);
    } else {
      switch( sortColumn ) {
        case 'name' :
        case 'lastEditor':
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

  getApplications(baseUrl: string) {
    this.appsService.getAppsList(baseUrl).subscribe( apps => {
      this.appsList = apps;
      // this.getAppsList(data);
    });
  }

  getVmList(baseUrl: string) {
    this.vmService.getVmList(baseUrl).subscribe( vms => {
      this.vmList = vms;
    });
  }

  getAppName(id: string) {
    for (let app of this.appsList) {
      if (id === app.id) {
        return app.name;
      }
    }
  }

  getVmName(id: string): void {
    for (let vm of this.vmList) {
      if (id === vm.id) {
        return vm.name;
      }
    }
  }

  virtueStatus(id: string) {
    this.virtuesService.toggleVirtueStatus(this.baseUrl, id).subscribe(data => {
      this.virtue = data;
    });
    this.resetRouter();
    this.router.navigate(['/virtues']);
  }

  deleteVirtue(id: string) {
    // console.log('deleting ' + id);
    this.virtuesService.deleteVirtue(this.baseUrl, id);
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

    const dialogResults = dialogRef.componentInstance.dialogEmitter.subscribe(data => {
      console.log('Dialog Emitter: ' + data);
      if (type === 'delete') {
        this.deleteVirtue(data);
      }
    });
  }
}
