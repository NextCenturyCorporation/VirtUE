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
  selector: 'app-gen-list',
  providers: [ ApplicationsService, BaseUrlService, VirtuesService, VirtualMachineService ],
  templateUrl: './gen-list.component.html',
  styleUrls: ['./gen-list.component.css']
})

export class GeneralListComponent implements OnInit {

  title: string;
  items = [];
  
  allUsers = [];
  allVirtues = [];
  allVms = [];
  allApps = [];

  baseUrl: string;
  // these are the default properties the list sorts by
  sortColumn: string = 'name';
  sortType: string = 'enabled';
  sortValue: any = '*';
  sortBy: string = 'asc';

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

      this.pullUsers();
      this.pullVirtues();
      this.pullVms();
      this.pullApplications();
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
      this.pullData();
    }, 400);
  }

  enabledItemList(sortType: string, enabledValue: any, sortBy) {
    console.log('enabledItemList() => ' + enabledValue);
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
      switch( sortColumn ) {
        case 'name' :
        case 'lastEditor':
        case 'enabled':
          this.sortBy = 'asc';
          break;
        case 'date':
          this.sortBy = 'desc';
          break;
        this.sortColumn = sortColumn;
      }
    }
  }

  reverseSorting(currentSortDirection: string) {
    if (currentSortDirection === 'asc') {
      this.sortBy = 'desc';
    } else {
      this.sortBy = 'asc';
    }
  }

  pullApplications() {
    this.appsService.getAppsList(this.baseUrl).subscribe( apps => {
      this.allApps = apps;
    });
  }

  pullVms() {
    this.vmService.getVmList(this.baseUrl).subscribe( vms => {
      this.allVms = vms;
    });
  }

  pullVirtues() {
    this.virtuesService.getVirtues(this.baseUrl).subscribe( virtueList => {
      this.allVirtues = virtueList;
    });
  }

  pullUsers( baseUrl: string ): void {
    this.usersService.getUsers(baseUrl).subscribe(userList => {
      this.allUsers = userList;
    });
  }

  getAppName(id: string) {
    for (let app of this.allApps) {
      if (id === app.id) {
        return app.name;
      }
    }
  }

  getVmName(id: string): void {
    for (let vm of this.allVms) {
      if (id === vm.id) {
        return vm.name;
      }
    }
  }

  getVirtueName(id: string): void {
    for (let v of this.allVirtues) {
      if (id === v.id) {
        return v.name;
      }
    }
  }

  toggleVirtueStatus(id: string) {
    this.virtuesService.toggleVirtueStatus(this.baseUrl, id).subscribe();
    //data holds the item just toggled. I don't think we need it though.
    //data => {
    //   this.item = data;
    // });
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
