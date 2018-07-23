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

  baseUrl: string;
  title = 'Applications';
  filterValue = '*';
  apps = [];

  sortColumn: string = 'name';
  sortType: string = 'enabled';
  sortValue: any = '*';
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
      this.setBaseUrl(awsServer);
      this.getApplications();
    });
  }

  setBaseUrl(url: string) {
    this.baseUrl = url;
  }

  getApplications() {
    this.appsService.getAppsList(this.baseUrl)
    .subscribe( appsList => {
      this.apps = appsList;
      this.totalApps = appsList.length;
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

  sortAppColumns(sortColumn: string, sortBy: string) {
    if (this.sortColumn === sortColumn) {
      this.sortListBy(sortBy);
    } else {
      if (sortColumn === 'name') {
        this.sortBy = 'asc';
        this.sortColumn = sortColumn;
      } else if (sortColumn === 'os') {
        this.sortBy = 'asc';
        this.sortColumn = sortColumn;
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
}
