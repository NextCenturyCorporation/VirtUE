import { Component, Injector, Input, OnDestroy, OnInit } from '@angular/core';
import { HttpClient, HttpEvent, HttpHeaders, HttpInterceptor, HttpHandler, HttpRequest } from '@angular/common/http';
import { MatDialog, MatDialogRef } from '@angular/material';
import { Router } from '@angular/router';
import { Observable } from 'rxjs/Observable';
import { filter } from 'rxjs/operators';

import { DialogsComponent } from '../../dialogs/dialogs.component';

import { ActiveClassDirective } from '../../shared/directives/active-class.directive';
import { Virtue } from '../../shared/models/virtue.model';
import { VirtualMachine } from '../../shared/models/vm.model';
import { Application } from '../../shared/models/application.model';

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

export class VirtueListComponent implements OnInit, OnDestroy {
  virtue: Virtue[];
  title = 'Virtues';
  virtues = [];
  vmList = [];
  appsList = [];
  virtueEnabled: boolean;
  baseUrl: string;
  virtueTotal: number;
  os: Observable<Array<VirtuesService>>;

  constructor(
    private router: Router,
    private appsService: ApplicationsService,
    private baseUrlService: BaseUrlService,
    private virtuesService: VirtuesService,
    private vmService: VirtualMachineService,
    public dialog: MatDialog,
  ) {
    this.router.routeReuseStrategy.shouldReuseRoute = function() {
      return false;
    };
  }

  ngOnInit() {
    this.baseUrlService.getBaseUrl().subscribe( res => {
      let awsServer = res[0].aws_server;
      this.getBaseUrl(awsServer)
      this.getVirtues(awsServer);
      this.getApplications(awsServer);
      this.getVmList(awsServer);
    });
  }

  ngOnDestroy() {
  }


  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    console.log(req);
    return next.handle(req);
  }

  getBaseUrl(url: string) {
    this.baseUrl = url;
  }

  resetRouter() {
    setTimeout(() => {
      this.router.navigated = false;
    }, 1000);
    this.router.navigate(['/virtues']);
  }

  getVirtues(baseUrl: string) {
    this.virtuesService.getVirtues(baseUrl).subscribe( virtues => {
      this.virtues = virtues;
    });
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

  getAppsList(list: any[]) {
    this.appsList = list;
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
      console.log('Dialog Emitter: ' + data);
      if (type === 'delete') {
        this.deleteVirtue(data);
      }
    });
  }

  deleteVirtue(id: string) {
    console.log('deleting ' + id);
    this.virtuesService.deleteVirtue(this.baseUrl, id);
    this.resetRouter();
  }

  virtueStatus(id: string, isEnabled: boolean) {
    if (isEnabled) {
      this.virtueEnabled = false;
    } else {
      this.virtueEnabled = true;
    }
    console.log('Virtue is enabled: ' + this.virtueEnabled);
    this.virtuesService.toggleVirtueStatus(this.baseUrl, id, isEnabled);
    this.resetRouter();
  }
}
