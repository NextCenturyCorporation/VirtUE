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
      this.setBaseUrl(awsServer);
      this.getVirtues();
      this.getApplications();
      this.getVmList();
    });
    this.refreshData();
    this.resetRouter();
  }

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    return next.handle(req);
  }

  setBaseUrl(url: string) {
    this.baseUrl = url;
  }

  resetRouter() {
    setTimeout(() => {
      this.router.navigated = false;
    }, 1000);
  }

  refreshData() {
    setTimeout(() => {
      this.getVirtues();
    }, 1000);
  }

  getVirtues() {
    this.virtuesService.getVirtues(this.baseUrl).subscribe( virtueList => {
      this.virtues = virtueList;
      this.totalVirtues = virtueList.length;
    });
  }


  enabledVirtueList(sortType: string, enabledValue: any, sortBy) {
    console.log('enabledVirtueList() => ' + enabledValue);
    if (this.sortValue !== enabledValue) {
      this.sortBy = 'asc';
    } else {
      this.reverseSorting(sortBy);
    }
    this.sortValue = enabledValue;
    // this.sortType = sortType;
  }

  setColumnSortDirection(sortColumn: string, sortBy: string) {
    if (this.sortColumn === sortColumn) {
      this.reverseSorting(sortBy);
    } else {
      if (sortColumn === 'name') {
        this.sortBy = 'asc';
      } else if (sortColumn === 'lastEditor') {
        this.sortBy = 'asc';
      } else if (sortColumn === 'enabled') {
        this.sortBy = 'asc';
      } else if (sortColumn === 'date') {
        this.sortBy = 'desc';
      }
        this.sortColumn = sortColumn;
    }
  }

  reverseSorting(currentSortDirection: string) {
    if (currentSortDirection === 'asc') {
      this.sortBy = 'desc';
    } else {
      this.sortBy = 'asc';
    }
  }

  getApplications() {
    this.appsService.getAppsList(this.baseUrl).subscribe( apps => {
      this.appsList = apps;
    });
  }

  getVmList() {
    this.vmService.getVmList(this.baseUrl).subscribe( vms => {
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

  toggleVirtueStatus(id: string) {
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
