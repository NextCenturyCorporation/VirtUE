import { Component, OnInit, ViewChild } from '@angular/core';
import { HttpEvent, HttpHandler, HttpRequest } from '@angular/common/http';
import { Location } from '@angular/common';
import { FormControl } from '@angular/forms';
import { MatDialog } from '@angular/material';
import { ActivatedRoute, Router } from '@angular/router';
import { Observable } from 'rxjs/Observable';

import { BaseUrlService } from '../../services/baseUrl.service';
import { ItemService } from '../../services/item.service';
// import { ApplicationsService } from '../../services/applications.service';
// import { VirtuesService } from '../../services/virtues.service';
// import { VirtualMachineService } from '../../services/vm.service';
// import { UsersService } from '../../services/users.service';

// import { VmModalComponent } from '../vm-modal/vm-modal.component';

import { User } from '../../models/user.model';
import { Virtue } from '../../models/virtue.model';
import { VirtualMachine } from '../../models/vm.model';
import { Application } from '../../models/application.model';
import { Item } from '../../models/item.model';
import { DictList } from '../../models/dictionary.model';
import { Mode } from '../../enums/enums';

import { GenericPageComponent } from '../gen-page/gen-page.component';


@Component({
  providers: [ BaseUrlService, ItemService ]
})
export abstract class GenericFormComponent extends GenericPageComponent {

  // TODO I have no idea what this does, but it was called "adUserCtrl" in the
  // original user file, before refactor.
  itemForm: FormControl;

  //Note:
  //  when creating, item.id is empty.
  //  When editing, item.id holds the id of the virtue being edited.
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

  constructor(
    protected parentDomain: string,
    protected activatedRoute: ActivatedRoute,
    router: Router,
    baseUrlService: BaseUrlService,
    itemService: ItemService,
    dialog: MatDialog
  ) {
    super(router, baseUrlService, itemService, dialog);
    // super(router, baseUrlService, usersService, virtuesService, vmService, appsService, dialog);
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
        // quits and returns to /{parentDomain}, and then for some reason re-calls the
        // constructor for the form component it just left. Which leads here and
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

    this.cmnComponentSetup();
  }

  getChildrenListHTMLstring(item: Item) {
    return item.childNamesHTML;
  }

  //overrides parent
  onPullComplete() {
    if (this.mode !== Mode.CREATE) {// no data to load if creating a new one.
      this.buildItem();
    }
  }


  buildItem() {
  let tempItem = this[this.datasetName].get(this.item.id);
    if (tempItem){
      this.item = tempItem;
      this.updateUnconnectedFields();
      this.updateChildList();
      this.resetRouter();
    }
    else {
      console.log("No item with ID", this.item.id, "found in dataset", this.datasetName + ".");
      //TODO let the user know it didn't load
      this.cancel();
    }
  }

  //if nothing is passed in, we just want to populate item.children
  updateChildList( newVmIDs? : string[] ) {

    if (newVmIDs instanceof Array) {
      this.item.childIDs = newVmIDs;
    }

    this.item.buildChildren(this[this.childDatasetName]);
  }

  createOrUpdate() {
    //collects/updates data for and in the item, in preparation for saving.
    if ( ! this.finalizeItem()) {
      console.log("Item not valid."); //TODO give useful error message
    }
    console.log(this.item);
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
    this.itemService.updateItem(this.serviceConfigUrl, this.item.id, JSON.stringify(this.item)).subscribe(
      data => {
        this.resetRouter();
        this.router.navigate([this.parentDomain]);
      },
      error => {
        console.log(error);
      });
  }

  //saves the selected settings as a new item
  createItem() {
    this.itemService.createItem(this.serviceConfigUrl, JSON.stringify(this.item)).subscribe(
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


  //create and fill the fields the backend expects to see, record any
  //uncollected inputs, and check that the item is valid to be saved
  abstract finalizeItem(): boolean;

  //can be overridden, if anything needs to be done manually upon item load.
  //currently overridden in virtue
  updateUnconnectedFields(): void {};
}
