import { Component, OnInit, ViewChild } from '@angular/core';
import { HttpEvent, HttpHandler, HttpRequest } from '@angular/common/http';
import { Location } from '@angular/common';
import { FormControl } from '@angular/forms';
import { MatDialog } from '@angular/material';
import { ActivatedRoute, Router } from '@angular/router';
import { Observable } from 'rxjs/Observable';

import { ApplicationsService } from '../../services/applications.service';
import { BaseUrlService } from '../../services/baseUrl.service';
import { VirtuesService } from '../../services/virtues.service';
import { VirtualMachineService } from '../../services/vm.service';
import { UsersService } from '../../services/users.service';

// import { VmModalComponent } from '../vm-modal/vm-modal.component';

import { User } from '../../models/user.model';
import { Virtue } from '../../models/virtue.model';
import { VirtualMachine } from '../../models/vm.model';
import { Application } from '../../models/application.model';
import { Item } from '../../models/item.model';

import { GenericPageComponent } from '../gen-page/gen-page.component';


@Component({
  providers: [ ApplicationsService, BaseUrlService, VirtuesService, VirtualMachineService, UsersService ]
})
export abstract class GenericFormComponent extends GenericPageComponent {

  itemForm: FormControl;
  activeClass: string;
  errorMsg: any;

  serviceCreateFunc: (baseUrl: string, jsonBody: string)=> Observable<any>;
  serviceUpdateFunc: (baseUrl: string, id: string, jsonBody: string)=> Observable<any>;

  //Note:
  //  when creating, virtue id is empty.
  //  When editing, virtue id holds the id of the virtue being edited.
  //  When duplicating, item.id holds the id of the old virtue being duplicated.
  //  New IDs for creation and duplication are generated server-side.
  item: Item;

  mode : string; //"c" if creating new virtue, "e" for editing existing, "d" for creating a duplicate.
  actionName: string; //for the html - 'Create', 'Edit', or 'Duplicate'

  //data on existing virtue (obv. purpose on edit-page, but holds base virtues values when
  //duplicating, and is empty when creating)
  itemData = {};

  // The set of all apps available across the Savior/VirtUE system.
  // Not what's installed on any particular vm.
  appsList = [];

  //not used, for use later in Inter-virtue sttings I think.
  users: User[];
  virtues: Virtue[];

  // protected location: Location;
  constructor(
    protected parentDomain: string,
    protected activatedRoute: ActivatedRoute,
    router: Router,
    baseUrlService: BaseUrlService,
    usersService: UsersService,
    virtuesService: VirtuesService,
    vmService: VirtualMachineService,
    appsService: ApplicationsService,
    dialog: MatDialog
  ) {
    super(router, baseUrlService, usersService, virtuesService, vmService, appsService, dialog);
    this.setMode();
    //set up empty, will get filled in ngOnInit if not mode is not 'create'

    this.itemData = {};

    this.itemForm = new FormControl();

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
      if (this.router.routerState.snapshot.url === this.parentDomain) {
        // apparently any time an error happens on this page, the system
        // quits and returns to /virtues, and then for some reason re-calls the
        // constructor for CreateEditVirtueComponent. Which leads here and then
        // breaks because the URL is wrong. Strange.
        return false;
      }
      console.log("ERROR: Can't decipher URL; Something about \
the routing system has changed. Returning to virtues page.\n       Expects something like \
"+this.parentDomain+"/create, "+this.parentDomain+"/duplicate/key-value, or "+this.parentDomain+"/edit/key-value,\
 but got: \n       " + this.router.routerState.snapshot.url);
      this.router.navigate([this.parentDomain]);
      return false;
    }
    return true;
  }

  ngOnInit() {
    if (this.mode === "e" || this.mode === "d") {//if "d" or "e"
      this.item.id = this.activatedRoute.snapshot.params['id'];
    }
    this.baseUrlService.getBaseUrl().subscribe(res => {
      //remember the stuff inside this block has to wait for a response before
      //getting run.
      let awsServer = res[0].aws_server;
      this.setBaseUrl(awsServer);

      this.pullData();
    });

    this.resetRouter();
  }

  //This should only be pulled the first time it loads.
  //perhaps we could update the datasets more often (when? every x seconds?)
  //but we certainly don't want to update the item, i.e. re-pulling the
  //original data and overwriting the edits we're trying to make.
  //wait 500ms before setting item, to give pullDatasets time to get back.
  //TODO that does not sound safe.
  //Indeed, I had it set to 300ms and occasionally (rarely), it'd fail.
  pullData() {
      this.pullDatasets();
      //do we want to update the duplicate? I don't think so.
      //But if I change it here I'll need to load the duplicate separately the first time
      if (this.mode !== "c") {//if "d" or "e"
        setTimeout(() => {
          this.pullItemData(this.item.id);
          console.log(this.item)
        }, 500);
      }
  }

  createOrUpdate() {
    if (this.mode === 'd' || this.mode === 'c') {
      this.createItem();
    }
    else if ( this.mode === 'e') {
      this.updateItem();
    }
    else {
      console.log("Could not save or update - mode not valid. Mode set to: ", this.mode);
    }
  }

  toggleItemStatus() {
    this.item.enabled = !this.item.enabled;
  }

  cancel() {
    this.router.navigate([this.parentDomain]);
  }

  //saves your edits to the backend
  updateItem(): void {
    this.finalizeItem();

    let body = this.item.getRepresentation();

    console.log("**", body);
    this.virtuesService.updateVirtue(this.baseUrl, this.item.id, JSON.stringify(body)).subscribe(
      data => {
        this.resetRouter();
        this.refreshData();
        // this.router.navigate([this.parentDomain]);
      },
      error => {
        console.log(error);
      });
  }

  //saves the selected settings as a new item
  createItem() {
    this.finalizeItem();

    let body = this.item.getRepresentation();

    this.serviceCreateFunc(this.baseUrl, JSON.stringify(body)).subscribe(
      data => {
        this.resetRouter();
        this.router.navigate([this.parentDomain]);
      },
      error => {
        console.log(error.message);
      });
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

  //this does any last updating before the item being worked on is saved to the backend.
  //like for virtues, the color needs to be taken from the virtue-settings panel and saved to the item.
  abstract finalizeItem();

  abstract pullItemData(id: string);
}
