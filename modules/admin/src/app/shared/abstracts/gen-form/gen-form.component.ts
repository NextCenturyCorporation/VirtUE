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
import { DictList } from '../../models/dictionary.model';
import { Mode } from '../../enums/mode.enum';

import { GenericPageComponent } from '../gen-page/gen-page.component';


@Component({
  providers: [ ApplicationsService, BaseUrlService, VirtuesService, VirtualMachineService, UsersService ]
})
export abstract class GenericFormComponent extends GenericPageComponent {

  // I have no idea what this does, but it was called "adUserCtrl" in the
  // original user file, before refactor.
  itemForm: FormControl;

  // activeClass: string;
  // errorMsg: any;

  serviceCreateFunc: (baseUrl: string, jsonBody: string)=> Observable<any>;
  serviceUpdateFunc: (baseUrl: string, id: string, jsonBody: string)=> Observable<any>;

  //Note:
  //  when creating, virtue id is empty.
  //  When editing, virtue id holds the id of the virtue being edited.
  //  When duplicating, item.id holds the id of the old virtue being duplicated.
  //  New IDs for creation and duplication are generated server-side.
  item: Item;

  mode : Mode; //"c" if creating new virtue, "e" for editing existing, "d" for creating a duplicate.
  actionName: string; //for the html - 'Create', 'Edit', or 'Duplicate'

  //data on existing virtue (obv. purpose on edit-page, but holds base virtues values when
  //duplicating, and is empty when creating).
  //Not used any more, might be helpful to hold onto though.
  itemData = {};

  //holds the name of the relevant dataset for the class;
  //  i.e., in virtue.component, it should be set to 'allVms'
  //Must be set in constructor of derived class.
  //Can't hold direct link because that reference won't be updated when
  //the dataset is pulled or re-pulled
  datasetName: string;
  childDatasetName: string;

  //holds the class of the item being edited/created.
  //Must be set in constructor of derived class.
  classType: any;

  //This was declared in virtues, vms, and apps, but not used anywhere
  //that I could tell.
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

   //originally this was only called in addUser's constructor, but in Virtues it was called in create, edit, and duplicate.
   //I don't know what it does, but it wouldn't persist once you leave the creation screen anyway. So let's make it every time.
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
    this.mode = Mode.CREATE;

    //Parse url, making sure it's set up the expected way.
    let urlValid = true;

    let route = url.split('/');
    if (route[0] !== this.parentDomain.substr(1)) {
      //something about the routing system has changed.
      urlValid = false;
    }
    if (route[1] === 'create') {
        this.mode = Mode.CREATE;
        this.actionName = "Create";
    } else if (route[1] === 'edit') {
        this.mode = Mode.EDIT;
        this.actionName = "Edit";
    } else if (route[1] === 'duplicate') {
        this.mode = Mode.DUPLICATE;
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
    if (this.mode === Mode.EDIT || this.mode === Mode.DUPLICATE) {
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

  //Data should only be pulled the first time the page loads.
  //If some sort of refresh button is added, implement the commented out function
  //and uncomment it.
  pullData() {
    // this.emptyDatasets(); //should empty the datasets built via updateFuncQueue
    this.pullDatasets();

    if (this.mode !== Mode.CREATE) {//if "d" or "e"
      this.buildData();
    }
  }

  buildData() {
    //try every 50ms for 3s or until the item is found.
    //This is called immediately after the request to pull datasets is sent,
    //and so the item's data won't be available for a short period, usually ~200ms.
    this.getItem(this.item.id, 50, 0, 3000);
  }


  //call a recursive function which will attempt to load the item repeatedly
  //for the specified number of milliseconds, waiting brifly between each attempt.
  //If the timeout is reached,
  getItem(id: string, delay: number, waited:number, timeout: number) {
    //try to get item
    let tempItem = this[this.datasetName].get(id);

    if (tempItem === undefined && timeout > 0) {
      //wait a bit and try again
      setTimeout(() => {
        waited += delay;
        this.getItem(this.item.id, delay, waited, timeout-delay);
      }, delay);
      return;
    }
    else if (timeout <= 0) {
      console.log("Waited:", waited + "ms.");
      tempItem = this[this.datasetName].get(id);
      this.setUpItem(tempItem, id);
      return;
    }

    console.log("Waited:", waited+ "ms.");
    this.setUpItem(tempItem);
  }

  setUpItem(tempItem, id?:string) {
    if (tempItem){
      this.item = tempItem;
      this.updateUnconnectedFields();
      this.updateChildList();
      this.resetRouter();
    }
    else {
      console.log("Unable to load data on item with ID:", id);
      //TODO do something, let the user know it didn't load
      this.cancel();
    }
  }

  //if nothing is passed in, we just want to populate item.children
  updateChildList( newVmIDs? : string[] ) {
    this.item.children = new DictList<Item>(); //TODO

    if (newVmIDs instanceof Array) {
      this.item.childIDs = newVmIDs;
    }

    for (let childID of this.item.childIDs) {
      let child: Item = this[this.childDatasetName].get(childID);
      if (child) {
        this.item.children.add(childID, child);
      }
      else {
        console.log("child ID in item not found in dataset. I.e., if this is for a user, \
it has a virtue ID attached to it which doesn't exist in the backend data.")
      }
    }
  }

  createOrUpdate() {
    if (this.mode === Mode.DUPLICATE || this.mode === Mode.CREATE) {
      this.createItem();
    }
    else if ( this.mode === Mode.EDIT) {
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

    // console.log("**", body);
    this.virtuesService.updateVirtue(this.baseUrl, this.item.id, JSON.stringify(body)).subscribe(
      data => {
        this.resetRouter();
        // this.refreshData();
        this.router.navigate([this.parentDomain]);
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

  //this does class-specific actions, saving or checking various fields
  //before the item is saved to the backend
  //like for virtues, the color needs to be taken from the virtue-settings
  //panel and saved to the item.
  //returns true iff the item is valid and can be saved.
  abstract finalizeItem(): boolean;

  //can be overridden, if anything needs to be done manually upon item load.
  //currently overridden in virtue
  updateUnconnectedFields(): void {};
}
