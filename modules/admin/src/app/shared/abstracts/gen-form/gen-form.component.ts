import { Component, OnInit, ViewChild } from '@angular/core';
import { HttpEvent, HttpHandler, HttpRequest } from '@angular/common/http';
import { Location } from '@angular/common';
import { FormControl } from '@angular/forms';
import { MatDialog } from '@angular/material';
import { ActivatedRoute, Router } from '@angular/router';
import { Observable } from 'rxjs/Observable';

import { BaseUrlService } from '../../services/baseUrl.service';
import { ItemService } from '../../services/item.service';

import { DialogsComponent } from '../../../dialogs/dialogs.component';

import { Item } from '../../models/item.model';
import { DictList } from '../../models/dictionary.model';
import { RowOptions } from '../../models/rowOptions.model';
import { Column } from '../../models/column.model';
import { Mode } from '../../enums/enums';

import { GenericPageComponent } from '../gen-page/gen-page.component';
import { GenericTableComponent } from '../gen-table/gen-table.component';
import { GenericModalComponent } from '../../../modals/generic-modal/generic.modal';
import { VirtueModalComponent } from '../../../modals/virtue-modal/virtue-modal.component';
import { VmModalComponent } from '../../../modals/vm-modal/vm-modal.component';


@Component({
  providers: [ BaseUrlService, ItemService ]
})
export abstract class GenericFormComponent extends GenericPageComponent implements OnInit {

  //  TODO currently not used, but could/should be eventually, time-permitting.
  //  itemForm: FormControl;

  // Note:
  //   when creating, item.id is empty.
  //   When editing, item.id holds the id of the virtue being edited.
  //   When duplicating, item.id holds the id of the old virtue being duplicated.
  //   New IDs for creation and duplication are generated server-side.
  item: Item;

  noDataMessage: string;

  // what the user is doing to the item: {CREATE, EDIT, DUPLICATE}
  // Holds the strings 'Create', 'Edit', or 'Duplicate' resp., for display to the user
  mode: Mode;

  // The table showing what children have been added to this item
  @ViewChild(GenericTableComponent) table: GenericTableComponent;

  // top-domain for child type. So for user.component, this would be '/virtues'
  childDomain: string;

  // holds the name of the relevant dataset for the class;
  //   i.e., in virtue.component, it should be set to 'allVms'
  // Must be set in constructor of derived class.
  // Can't hold direct link because that reference won't be updated when
  // the dataset is pulled or re-pulled
  datasetName: string;
  childDatasetName: string;

  constructor(
    protected parentDomain: string,
    protected activatedRoute: ActivatedRoute,
    router: Router,
    baseUrlService: BaseUrlService,
    itemService: ItemService,
    dialog: MatDialog
  ) {
    super(router, baseUrlService, itemService, dialog);
    this.setMode();

    //  see note by declaration
    //  this.itemForm = new FormControl();

    // override the route reuse strategy
    // Tell angular to load a new component every time a URL that needs this component loads,
    // even if the user has been on this page before.
    // TODO May want to look into changing this for these form pages, so a user who leaves
    // a page mid-edit could return back and finish.
    this.router.routeReuseStrategy.shouldReuseRoute = function() {
      return false;
    };

  }

  // This checks the current routing info (the end of the current url)
  // and uses that to set what mode (create/edit/duplicate) the page
  //  ought to be in.
  setMode() {
    let url = this.router.routerState.snapshot.url;
    if (url[0] === '/') {
      url = url.substr(1);
    }
    this.mode = Mode.CREATE;

    // Parse url, making sure it's set up the expected way.
    let urlValid = true;

    let route = url.split('/');
    if (route[0] !== this.parentDomain.substr(1)) {
      // something about the routing system has changed.
      urlValid = false;
    }
    if (route[1] === 'create') {
        this.mode = Mode.CREATE;
    } else if (route[1] === 'edit') {
        this.mode = Mode.EDIT;
    } else if (route[1] === 'duplicate') {
        this.mode = Mode.DUPLICATE;
    } else {
        // something about the routing system has changed.
        urlValid = false;
    }
    if (!urlValid) {
      if (this.router.routerState.snapshot.url === this.parentDomain) {
        // apparently any time an error happens on this page, the system
        // quits and returns to /{parentDomain}, and then for some reason re-calls the
        // constructor for the form component it just left. Which leads here and
        // breaks because the URL is wrong. Strange. So don't print out the below
        // error on those cases.
        return false;
      }
      console.log("ERROR: Can't decipher URL; Something about \
the routing system has changed. Returning to virtues page.\n       Expects something like \
" + this.parentDomain + "/create, " + this.parentDomain + "/duplicate/key-value, or " + this.parentDomain + "/edit/key-value,\
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
    this.fillTable();
  }

  fillTable(): void {
    if (this.table === undefined) {
      return;
    }

    this.table.setUp({
      cols: this.getColumns(),
      opts: this.getOptionsList(),
      coloredLabels: this.hasColoredLabels(),
      filters: [], // don't allow filtering on the form's child table. ?
      tableWidth: this.getTableWidth(),
      noDataMsg: this.getNoDataMsg(),
      hasCB: false
    });
  }

  getChildrenListHTMLstring(item: Item) {
    return item.childNamesHTML;
  }

  // overrides parent
  onPullComplete() {
    if (this.mode !== Mode.CREATE) {//  no data to load if creating a new one.
      this.buildItem();
    }
    this.table.items = this.item.children.asList();
    this.setUpFormValues();
  }

  //  set up child form-pages' unique properties
  //  does nothing by default, overridden by user form
  setUpFormValues(): void {}

  buildItem() {
  let _item = this[this.datasetName].get(this.item.id);
    if (_item) {
      this.item = _item;
      this.updateUnconnectedFields();
      this.updateChildList();
      this.resetRouter();
    }
    else {
      console.log("No item with ID", this.item.id, "found in dataset", this.datasetName + ".");
      // TODO let the user know it didn't load
      this.cancel();
    }
  }

  // if nothing is passed in, we just want to populate item.children
  updateChildList( newVmIDs?: string[] ) {

    if (newVmIDs instanceof Array) {
      this.item.childIDs = newVmIDs;
    }

    this.item.buildChildren(this[this.childDatasetName]);
    this.table.items = this.item.children.asList();
  }

  createOrUpdate() {
    // collects/updates data for and in the item, in preparation for saving.
    if ( ! this.finalizeItem()) {
      console.log("Item not valid."); // TODO give useful error message
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

  // saves your edits to the backend
  updateItem(): void {
    let sub = this.itemService.updateItem(this.serviceConfigUrl, this.item.getID(), JSON.stringify(this.item)).subscribe(
      data => {
        this.resetRouter();
        this.router.navigate([this.parentDomain]);
      },
      error => {
        console.log(error);
      },
      () => {// when finished
        sub.unsubscribe();
      });
  }

  // saves the selected settings as a new item
  createItem() {
    let sub = this.itemService.createItem(this.serviceConfigUrl, JSON.stringify(this.item)).subscribe(
      data => {
        this.resetRouter();
        this.router.navigate([this.parentDomain]);
      },
      error => {
        console.log(error.message);
      },
      () => {// when finished
        sub.unsubscribe();
      });
  }

  /**
   copied from gen-list, could merge that together at some point if had extra time.
   this is a checker, if the user clicks 'remove' on one of the item's children.
   Could be improved/made more clear/distinguished from all the childrens' "activateModal" method.
  */
  openDialog(action: string, target: Item): void {
    let dialogRef = this.dialog.open(DialogsComponent, {
      width: '450px',
      data:  {
          actionType: action,
          targetObject: target
        }
    });

    dialogRef.updatePosition({ top: '15%', left: '36%' });

    //  control goes here after either "Ok" or "Cancel" are clicked on the dialog
    let sub = dialogRef.componentInstance.dialogEmitter.subscribe((targetObject) => {

      if (targetObject !== 0 ) {
        if (action === 'delete') {

          this.item.removeChild(targetObject.getID());

        }
      }
    },
    () => {},
    () => {// when finished
      sub.unsubscribe();
    });
  }

  /*this needs to be defined in each child, instead of here, because I can't find how to have each
  child hold a class as an attribute, to be used in a dialog.open method in a parent's function.
  So right now the children take care of the dialog.open method, and pass the
  MatDialogRef back. I can't type this as returning a MatDialogRef though
  without having to specify what modal class the dialog refers to (putting us
  back at the original issue), so this will have to be 'any' for now.
  */
  abstract getModal(
    params: {width: string, height: string, data: {id: string, selectedIDs: string[] }}
  ): any;

  // this brings up the modal to add/remove children
  activateModal(mode: string): void {
    let dialogHeight = 600;
    let dialogWidth = 800;

    let modalParams = {
      height: dialogHeight + 'px',
      width: dialogWidth + 'px',
      data: {
        id: this.item.getName(),
        selectedIDs: this.item.childIDs
      }
    };

    let dialogRef = this.getModal(modalParams);

    let sub = dialogRef.componentInstance.getSelections.subscribe((selectedVirtues) => {
      this.updateChildList(selectedVirtues);
    },
    () => {},
    () => {// when finished
      sub.unsubscribe();
    });
    let leftPosition = ((window.screen.width) - dialogWidth) / 2;

    dialogRef.updatePosition({ top: '5%', left: leftPosition + 'px' });

  }

  // overrides parent
  getOptionsList(): RowOptions[] {
    return [
      //  new RowOptions("Edit", () => true, (i:Item) => this.editItem(i)),
      // TODO look into this, perhaps we could have two modes on the form pages -
      // one for editing, one for viewing. So you could navigate away only when
      // you weren't in edit mode, and you'd never lose changes accidentally.
      // User will lose all work on form if they navigate away to other form
      // It'd be nice to let them do that though.
      new RowOptions("Remove", () => true, (i: Item) => this.openDialog('delete', i))
    ];
  }

  // overridden by virtue component
  getTableWidth(): number {
    return 9;
  }

  // used by many children to display their status
  formatStatus( item: Item ): string {
    return item.enabled ? 'Enabled' : 'Disabled';
  }

  getChildNamesHtml( item: Item) {
    return item.childNamesHTML;
  }

  editItem(i: Item) {
    if (this.childDomain) {
      this.router.navigate([this.childDomain + "/edit/" + i.getID()]);
    }
  }

  hasColoredLabels(): boolean {
    return false;
  }

  // create and fill the fields the backend expects to see, record any
  // uncollected inputs, and check that the item is valid to be saved
  abstract finalizeItem(): boolean;

  // can be overridden, if anything needs to be done manually upon item load.
  // currently overridden in virtue
  updateUnconnectedFields(): void {}

  abstract getColumns(): Column[];

  abstract getNoDataMsg(): string;

}