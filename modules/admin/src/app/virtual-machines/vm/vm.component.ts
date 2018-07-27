import { Component, Input, OnInit } from '@angular/core';
import { FormControl } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { MatDialog } from '@angular/material';
import { HttpEvent, HttpHandler, HttpRequest } from '@angular/common/http';
import { Observable } from 'rxjs/Observable';

import { Application } from '../../shared/models/application.model';
import { VirtualMachine } from '../../shared/models/vm.model';

import { BaseUrlService } from '../../shared/services/baseUrl.service';
import { ApplicationsService } from '../../shared/services/applications.service';
import { VirtualMachineService } from '../../shared/services/vm.service';

import { VmAppsModalComponent } from '../vm-apps-modal/vm-apps-modal.component';

@Component({
  selector: 'app-vm',
  templateUrl: './vm.component.html',
  providers: [ ApplicationsService, BaseUrlService, VirtualMachineService ]
})
export class VmComponent implements OnInit {

  vm: VirtualMachine;
  vmData: string;

  form: FormControl;

  baseUrl: string;

  mode : string; //"c" if creating new virtue, "e" for editing existing, "d" for creating a duplicate.
  actionName: string; //for the html - 'Create', 'Edit', or 'Duplicate'

  parentDomain: string;


  osList = [
    { 'name': 'Debian', 'os': 'LINUX' },
    { 'name': 'Windows', 'os': 'WINDOWS' }
  ];
  securityOptions = [
    { 'level': 'default', 'name': 'Default' },
    { 'level': 'email', 'name': 'Email' },
    { 'level': 'power', 'name': 'Power User' },
    { 'level': 'admin', 'name': 'Administrator' }
  ];

  constructor(
    private activatedRoute: ActivatedRoute,
    private appsService: ApplicationsService,
    private baseUrlService: BaseUrlService,
    private router: Router,
    private vmService: VirtualMachineService,
    public dialog: MatDialog
  ) {
    this.parentDomain = "/vm-templates";
    this.setMode();

    this.vm = new VirtualMachine(undefined);

    this.form = new FormControl();
  }

  ngOnInit() {
    if (this.mode == 'd' || this.mode == 'e') {
      this.vm.id = this.activatedRoute.snapshot.params['id'];
    }
    this.baseUrlService.getBaseUrl().subscribe(res => {
      let awsServer = res[0].aws_server;
      this.setBaseUrl(awsServer);
      if (this.mode == 'd' || this.mode == 'e') {
        this.pullVmData(this.vm.id);
      }
    });
  }

  //This checks the current routing info (the end of the current url)
  //and uses that to set what mode (create/edit/duplicate) the page
  // ought to be in.
  // Create new virtue: 'c', Edit virtue: 'e', Duplicate virtue: 'd'
  setMode() {
    // console.log(this.router.routerState.snapshot.url);
    let url = this.router.routerState.snapshot.url;
    if (url[0] === '/') {
      url = url.substr(1);
    }
    // a default, to let things exit smoothlly if anything goes wrong.
    this.mode = 'c';

    //Parse url, making sure it's set up the expected way.
    let urlValid = true;

    let route = url.split('/');
    if (route[0] !== this.parentDomain.substr(1)) { //substr to ignore the leading '/'
      //something about the routing system has changed.
      urlValid = false;
    }
    if (route[1] === 'create') {
        this.mode = 'c';
        this.actionName = "Create";
    } else if (route[1] === 'edit') {
        this.mode = 'e';
        this.actionName = "Edit";
    } else if (route[1] === 'duplicate') {
        this.mode = 'd';
        this.actionName = "Duplicate";
    } else {
        //something about the routing system has changed.
        urlValid = false;
    }
    if (!urlValid) {
      if (this.router.routerState.snapshot.url === this.parentDomain) {
        // apparently any time an error happens on this page, the system
        // quits and returns to /virtues, and then for some reason re-calls the
        // constructor for CreateEditVirtueComponent. Which leads here and then
        // breaks because the URL is wrong. Strange.
        return false;
      }
      console.log("ERROR: Can't decipher URL; Something about \
the routing system has changed. Returning to virtues page.\n       Expects something like \
/virtues/create, /virtues/duplicate/long-machine-key-value, or /virtues/edit/long-machine-key-value,\
 but got: \n       " + this.router.routerState.snapshot.url);
      // if this gets broken later, it could changed to use this.activatedRoute.routeConfig.path instead
      this.router.navigate([this.parentDomain]);
      return false;
    }
    return true;
  }

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    // console.log(req);
    return next.handle(req);
  }

  resetRouter() {
    setTimeout(() => {
      this.router.navigated = false;
    }, 1000);
  }

  refreshData() {
    setTimeout(() => {
      this.pullVmData(this.vm.id);
    }, 300);
  }

  setBaseUrl(url: string) {
    this.baseUrl = url;
  }

  pullVmData(id: string) {
    this.vmService.getVM(this.baseUrl, id).subscribe(
      data => {
        this.vmData = data;
        this.vm.os = data.os;
        this.vm.name = data.name;
        this.updateAppsList(data.applicationIds);
        this.vm.securityTag = data.securityTag;
        this.vm.enabled = data.enabled;
        console.log(this.vm.securityTag);
      }
    );
  }

  updateAppsList(newAppsList) {
    // loop through the selected VM list
    this.vm.apps = new Array<Application>();
    this.vm.appIDs = newAppsList;
    // console.log('vm's app list @ updatevm.apps(): ' + this.vm.appIds);
    const vmAppIDs = newAppsList;
    for (let appID of vmAppIDs) {
      this.appsService.getApp(this.baseUrl, appID).subscribe(
        appData => {
          this.vm.apps.push(appData);
        },
        error => {
          console.log(error.message);
        }
      );
    }
  }

  toggleVmStatus() {
    this.vm.enabled = !this.vm.enabled;
  }

  removeApp(id: string, index: number): void {
    this.vm.apps = this.vm.apps.filter(data => {
      return data.id !== id;
    });
    this.vm.appIDs.splice(index, 1);
  }

  activateModal(): void {
    let dialogRef = this.dialog.open(VmAppsModalComponent, {
      width: '750px',
      data: {
        selectedApps: this.vm.appIDs
      }
    });
    dialogRef.updatePosition({ top: '5%', left: '20%' });

    const apps = dialogRef.componentInstance.addApps.subscribe((dialogAppsList) => {
      this.updateAppsList(dialogAppsList);
    });

    dialogRef.afterClosed().subscribe(() => {
      apps.unsubscribe();
    });
  }

  createOrUpdate() {
    if (this.mode === 'd' || this.mode === 'c') {
      this.buildVM();
    }
    else if ( this.mode === 'e') {
      this.updateThisVM();
    }
    else {
      console.log("Could not save or update - mode not valid. Mode set to: ", this.mode);
    }
  }

  updateThisVM() {
    let body = {
      'name': this.vm.name,
      'os': this.vm.os,
      'loginUser': 'system',
      'enabled': this.vm.enabled,
      'applicationIds': this.vm.appIDs,
      'securityTag': this.vm.securityTag
    };
    this.vmService.updateVM(this.baseUrl, this.vm.id, JSON.stringify(body));
    this.resetRouter();
    this.router.navigate([this.parentDomain]);
  }

  buildVM() {
    let body = {
      'name': this.vm.name,
      'os': this.vm.os,
      'loginUser': 'system',
      'enabled': this.vm.enabled,
      'applicationIds': this.vm.appIDs,
      'securityTag': this.vm.securityTag
    };
    // console.log(body);
    this.vmService.createVM(this.baseUrl, JSON.stringify(body));
    this.resetRouter();
    this.router.navigate([this.parentDomain]);
  }

  cancel() {
    this.router.navigate([this.parentDomain]);
  }

}
