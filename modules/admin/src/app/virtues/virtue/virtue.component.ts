import { Component, OnInit, ViewChild } from '@angular/core';
import { HttpEvent, HttpHandler, HttpRequest } from '@angular/common/http';
import { Location } from '@angular/common';
import { FormControl } from '@angular/forms';
import { MatDialog } from '@angular/material';
import { ActivatedRoute, Router } from '@angular/router';
import { Observable } from 'rxjs/Observable';

import { ApplicationsService } from '../../shared/services/applications.service';
import { BaseUrlService } from '../../shared/services/baseUrl.service';
import { VirtuesService } from '../../shared/services/virtues.service';
import { VirtualMachineService } from '../../shared/services/vm.service';

import { VmModalComponent } from '../vm-modal/vm-modal.component';

import { User } from '../../shared/models/user.model';
import { Virtue } from '../../shared/models/virtue.model';
import { VirtualMachine } from '../../shared/models/vm.model';

import { VirtueSettingsComponent } from '../virtue-settings/virtue-settings.component';



@Component({
  selector: 'app-virtue',
  templateUrl: './virtue.component.html',
  // styleUrls: ['./edit-virtue.component.css'],
  providers: [ ApplicationsService, BaseUrlService, VirtuesService, VirtualMachineService ]
})

export class VirtueComponent implements OnInit {
  @ViewChild(VirtueSettingsComponent) settingsPane: VirtueSettingsComponent;

  virtueForm: FormControl;
  activeClass: string;
  baseUrl: string;
  errorMsg: any;

  //Note:
  //  when creating, virtue id is empty.
  //  When editing, virtue id holds the id of the virtue being edited.
  //  When duplicating, virtue.id holds the id of the old virtue being duplicated.
  //  New IDs for creation and duplication are generated server-side.
  virtue: Virtue;

  //not used, for use later in Inter-virtue sttings I think.
  users: User[];
  virtues: Virtue[];

  mode : string; //"c" if creating new virtue, "e" for editing existing, "d" for creating a duplicate.
  actionName: string; //for the html - 'Create', 'Edit', or 'Duplicate'

  parentDomain: string;

  //data on existing virtue (obv. purpose on edit-page, but holds base virtues values when
  //duplicating, and is empty when creating)
  virtueData = {};

  // The set of all apps available across the Savior/VirtUE system.
  // Not what's installed on any particular vm.
  appsList = [];

  constructor(
    private activatedRoute: ActivatedRoute,
    private router: Router,
    private appsService: ApplicationsService,
    private baseUrlService: BaseUrlService,
    private virtuesService: VirtuesService,
    private vmService: VirtualMachineService,
    private location: Location,
    public dialog: MatDialog
  ) {
    this.parentDomain = '/virtues'
    this.setMode();
    //set up empty, will get filled in ngOnInit if not mode is not 'create'
    this.virtue = new Virtue(undefined);

    this.virtueData = {};

    if (this.mode === "c") {
      this.virtue.color = "#cccccc";
    }

    this.virtueForm = new FormControl();

    // override the route reuse strategy
    this.router.routeReuseStrategy.shouldReuseRoute = function() {
      return false;
    };

  }

  //This checks the current routing info (the end of the current url)
  //and uses that to set what mode (create/edit/duplicate) the page
  // ought to be in.
  // Create new virtue: 'c', Edit virtue: 'e', Duplicate virtue: 'd'
  setMode() {
    let url = this.router.routerState.snapshot.url;
    if (url[0] === '/') {
      url = url.substr(1);
    }
    this.mode = 'c';

    //Parse url, making sure it's set up the expected way.
    let urlValid = true;

    let route = url.split('/');
    if (route[0] !== this.parentDomain.substr(1)) {
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
      if (this.router.routerState.snapshot.url === "/virtues") {
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
      this.router.navigate([this.parentDomain]);
      return false;
    }
    return true;
  }

  ngOnInit() {
    if (this.mode === "e" || this.mode === "d") {//if "d" or "e"
      this.virtue.id = this.activatedRoute.snapshot.params['id'];
    }
    this.baseUrlService.getBaseUrl().subscribe(res => {
      //remember the stuff inside this block has to wait for a response before
      //geing run.
      let awsServer = res[0].aws_server;
      this.setBaseUrl(awsServer);

      if (this.mode !== "c") {//if "d" or "e"
        this.pullVirtueData(this.virtue.id);
      }
      this.settingsPane.setColor(this.virtue.color);
      this.getAppsList();
    });

    this.resetRouter();
  }

  resetRouter() {
    setTimeout(() => {
      this.router.navigated = false;
    }, 1000);
  }

  refreshData() {
    // console.log("refreshData");
    setTimeout(() => {
      this.pullVirtueData(this.virtue.id);
    }, 1000);
  }

  setBaseUrl(url: string) {
    this.baseUrl = url;
  }

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    return next.handle(req);
  }

  updateVmList( newvmIDs : any[] ) {
    // loop through the selected VM list
    this.virtue.vms = [];
    this.virtue.childIDs = newvmIDs;
    for (let vmID of newvmIDs) {
      this.vmService.getVM(this.baseUrl, vmID).subscribe(
        vm => {
          this.virtue.vms.push(vm);
        },
        error => {
          console.log(error.message);
        }
      );
    }
  }

  pullVirtueData(id: string) {
    this.virtuesService.getVirtue(this.baseUrl, id).subscribe(vData => {
      if (! vData.color) {
        vData.color = vData.enabled ? "#BB00AA" : "#00DD33"
      }
      if (! vData.virtualMachineTemplateIds) {
        vData.virtualMachineTemplateIds = [];
      }
      if (! vData.version) {
        vData.version = '1.0';
      }
      this.virtueData = vData;
      this.virtue.name = vData.name;
      this.virtue.id = vData.id;
      this.virtue.color = vData.color;
      this.settingsPane.setColor(this.virtue.color);
      this.virtue.enabled = vData.enabled;
      this.virtue.version = vData.version;
      this.updateVmList(vData.virtualMachineTemplateIds);
    });
    this.resetRouter();
  }

  getAppsList() {
    this.appsService.getAppsList(this.baseUrl).subscribe(data => {
      this.appsList = data;
    });
  }

  getAppName(id: string) {
    const app = this.appsList.filter(data =>  id === data.id);
    if (id !== null) {
      return app[0].name;
    }
  }

  removeVm(id: string, index: number): void {
    this.virtue.vms = this.virtue.vms.filter(data => {
      return data.id !== id;
    });
    this.virtue.childIDs.splice(index, 1);
  }


  activateModal(): void {

    let dialogRef = this.dialog.open(VmModalComponent, {
      width: '750px',
      data: {
        selectedVms: this.virtue.childIDs
      }
    });
    dialogRef.updatePosition({ top: '5%', left: '20%' });

    const vms = dialogRef.componentInstance.addVms.subscribe((dialogvmIDs) => {
      this.updateVmList(dialogvmIDs);
    });

    // dialogRef.afterClosed().subscribe(() => {
    //   vms.unsubscribe();
    // });
  }

  createOrUpdate() {
    if (this.mode === 'd' || this.mode === 'c') {
      this.createVirtue();
    }
    else if ( this.mode === 'e') {
      this.updateThisVirtue();
    }
    else {
      console.log("Could not save or update - mode not valid. Mode set to: ", this.mode);
    }
  }

  updateThisVirtue(): void {
    let body = {
      'name': this.virtue.name,
      'version': this.virtue.version,
      'enabled': this.virtue.enabled,
      'color' : this.settingsPane.getColor(),
      'virtualMachineTemplateIds': this.virtue.childIDs
    };
    this.virtuesService.updateVirtue(this.baseUrl, this.virtue.id, JSON.stringify(body)).subscribe(
      data => {
        this.refreshData();
        this.router.navigate([this.parentDomain]);
      },
      error => {
        console.log(error);
      });
  }

  createVirtue() {
    if (! this.virtue.version) {
      this.virtue.version = '1.0';
    }

    let body = {
      'name': this.virtue.name,
      'version': this.virtue.version,
      'enabled': this.virtue.enabled,
      'color' : this.settingsPane.getColor(),
      'virtualMachineTemplateIds': this.virtue.childIDs
    };

    this.virtuesService.createVirtue(this.baseUrl, JSON.stringify(body)).subscribe(
      data => {
        this.resetRouter();
        this.router.navigate([this.parentDomain]);
      },
      error => {
        console.log(error.message);
      });

  }

  toggleVirtueStatus() {
    this.virtue.enabled = !this.virtue.enabled;
  }

  cancel() {
    this.router.navigate([this.parentDomain]);
  }

  // deleteVirtue(id): void {
  //   let dialogRef = this.dialog.open(DialogsComponent, {
  //     width: '450px'
  //   });
  //
  //   dialogRef.updatePosition({ top: '15%', left: '36%' });
  //
  //   dialogRef.afterClosed().subscribe(result => {
  //     // console.log('This dialog was closed');
  //   });
  // }
}
