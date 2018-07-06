import { Component, OnDestroy, OnInit } from '@angular/core';
import { HttpEvent, HttpHandler, HttpRequest } from '@angular/common/http';
import { FormControl } from '@angular/forms';
import { Router } from '@angular/router';
import { MatDialog } from '@angular/material';
import { Observable } from 'rxjs/Observable';

import { VmAppsModalComponent } from '../vm-apps-modal/vm-apps-modal.component';

import { ApplicationsService } from '../../shared/services/applications.service';
import { BaseUrlService } from '../../shared/services/baseUrl.service';
import { VirtualMachineService } from '../../shared/services/vm.service';

@Component({
  selector: 'app-vm-build',
  templateUrl: './vm-build.component.html',
  providers: [ ApplicationsService, BaseUrlService, VirtualMachineService ]
})
export class VmBuildComponent implements OnInit, OnDestroy {
  vmForm: FormControl;
  activeClass: string;
  baseUrl: string;
  osValue: string;
  selectedOS: string;
  securityTag: string;
  securityLevel: string;

  appList = [];
  selAppList = [];
  pageAppList = [];
  osList = [
    { 'name': 'Debian', 'os': 'LINUX' },
    { 'name': 'Windows', 'os': 'WINDOWS' }
  ];

  securityOptions = [
    { 'level': 'default', 'name': 'Default'} ,
    { 'level': 'email', 'name': 'Email' },
    { 'level': 'power',  'name': 'Power User' },
    { 'level': 'admin', 'name': 'Administrator' }
  ];

  constructor(
    private vmService: VirtualMachineService,
    private appsService: ApplicationsService,
    private baseUrlService: BaseUrlService,
    private router: Router,
    public dialog: MatDialog,
  ) {
    this.vmForm = new FormControl();
  }

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    console.log(req);
    return next.handle(req);
  }

  ngOnInit() {
    this.baseUrlService.getBaseUrl().subscribe(res => {
      let awsServer = res[0].aws_server;
        this.getBaseUrl(awsServer);
        this.getAppList(awsServer);
    });
  }

  ngOnDestroy() {
  }

  getBaseUrl(url: string) {
    this.baseUrl = url;
  }

  getAppList(baseUrl: string) {
    this.baseUrl = baseUrl;
    // loop through the selected VM list
    const selectedApps = this.pageAppList;
    // console.log('page Apps list @ getAppList(): ' + this.pageAppList);
    this.appsService.getAppsList(baseUrl)
      .subscribe(apps => {
        if (this.appList.length < 1) {
          for (let sel of selectedApps) {
            for (let app of apps) {
              if (sel === app.id) {
                this.appList.push(app);
                break;
              }
            }
          }
        } else {
          this.getUpdatedAppList(baseUrl);
        }
      });
  }

  getUpdatedAppList(baseUrl: string) {
    this.appList = [];
    this.appsService.getAppsList(baseUrl)
      .subscribe(apps => {
        for (let sel of this.pageAppList) {
          for (let app of apps) {
            if (sel === app.id) {
              this.appList.push(app);
              break;
            }
          }
        }
      });
  }

  removeApp(id: string, index: number): void {
    this.appList = this.appList.filter(data => {
      return data.id !== id;
    });
    this.pageAppList.splice(index, 1);
  }


  activateModal(): void {

    let dialogRef = this.dialog.open(VmAppsModalComponent, {
      width: '750px',
      data: {
        selectedApps: this.pageAppList
      }
    });
    console.log('Apps sent to dialog: ' + this.pageAppList);
    dialogRef.updatePosition({ top: '5%', left: '20%' });

    const apps = dialogRef.componentInstance.addApps.subscribe((data) => {
      this.selAppList = data;
      console.log('Apps from dialog: ' + this.selAppList);
      if (this.pageAppList.length > 0) {
        this.pageAppList = [];
      }
      this.pageAppList = this.selAppList;

      this.getAppList(this.baseUrl);
    });

    dialogRef.afterClosed().subscribe(() => {
      apps.unsubscribe();
    });
  }

  buildVirtualMachine(vmName: string, vmOs: string, vmSecurityTag: string) {
    let body = {
      'name': vmName,
      'os': vmOs,
      'loginUser': 'system',
      'enabled': true,
      'applicationIds': this.pageAppList,
      'securityTag': vmSecurityTag
    };
    console.log(body);
    this.vmService.createVM(this.baseUrl, JSON.stringify(body));
    this.router.navigate(['/vm']);
  }
}
