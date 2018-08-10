import { Component, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs/Observable';

import { AddVmAppComponent } from '../add-vm-app/add-vm-app.component';
import { DialogsComponent } from '../../dialogs/dialogs.component';

import { ApplicationsService } from '../../shared/services/applications.service';
import { BaseUrlService } from '../../shared/services/baseUrl.service';

@Component({
  selector: 'app-vm-apps-list',
  providers: [ ApplicationsService, BaseUrlService ],
  templateUrl: './vm-apps-list.component.html'
})
export class VmAppsListComponent implements OnInit {

  title = 'Applications';
  filterValue = '*';
  apps = [];

  sortColumn: string = 'name';
  sortType: string = 'enabled';
  sortValue: string = '*';
  sortBy: string = 'asc';
  totalApps: number;
  // appsfilter: string;

  file: string;
  url: string;

  constructor(
    private route: ActivatedRoute,
    private appsService: ApplicationsService,
    private baseUrlService: BaseUrlService,
    public dialog: MatDialog
  ) { }

  ngOnInit() {
    this.baseUrlService.getBaseUrl().subscribe(res => {
      let awsServer = res[0].aws_server;
        this.getApplications(awsServer);
    });
  }

  getApplications(baseUrl: string) {
    this.appsService.getAppsList(baseUrl)
    .subscribe( appsList => {
      this.apps = appsList;
      this.totalApps = appsList.length;
    });
  }

  updateStatus(id: string): void {
    const app = this.apps.filter(data => data['id'] === id);
    app.map((_, i) => {
      app[i].enabled ? app[i].enabled = false : app[i].enabled = true;
      console.log(app);
    });

  }

  listFilter(status: any) {
    console.log('filterValue = ' + status);
    this.filterValue = status;
    this.totalApps = this.apps.length;
  }

  openDialogPrompt(id, type, text): void {
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

  openAppsDialog(): void {
    let dialogRef = this.dialog.open(AddVmAppComponent, {
      width: '480px',
      data: { file: this.file, url: this.url }
    });

    dialogRef.afterClosed().subscribe(result => {
      console.log('The dialog was closed');
    });
  }

  
  setColumnSort(sortColumn: string, sortBy: string) {
    if (this.sortColumn === sortColumn) {
      this.reverseSortDirection(sortBy);
    } else {
      this.sortBy = 'asc';
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

}
