import { Component, OnInit, ViewChild, ViewChildren, QueryList } from '@angular/core';
import { HttpEvent, HttpHandler, HttpRequest } from '@angular/common/http';
import { Location } from '@angular/common';
import { FormGroup, FormBuilder } from '@angular/forms';
import { MatDialog } from '@angular/material';
import { MatTabsModule } from '@angular/material/tabs';
import { ActivatedRoute, Router } from '@angular/router';
import { Observable } from 'rxjs/Observable';

import { BaseUrlService } from '../../../services/baseUrl.service';
import { DataRequestService } from '../../../services/dataRequest.service';

import { DialogsComponent } from '../../../../dialogs/dialogs.component';

import { Item } from '../../../models/item.model';

import { GenericTabbedFormComponent } from '../gen-form.component';

import { VirtueModalComponent } from '../../../../modals/virtue-modal/virtue-modal.component';
import { VmModalComponent } from '../../../../modals/vm-modal/vm-modal.component';

import { Mode } from './../mode.enum';
import { DatasetNames } from '../../gen-data-page/datasetNames.enum';

/**
 * @class __
 * This class represents a detailed view of a single [[Item]].
 *
 *
 * It can be in one of several modes ([[Mode]]): view, edit, duplicate, and create.
 *  - Create doesn't load any previous data.
 *  - View loads data, but makes the page uneditable.
 *  - Edit loads data and is editable. Changes overwrite the input item.
 *  - Duplicate loads data and lets the user edit it, but saves
 *      it as a new Item, rather than overwriting the input item.
 *
 * Each type of form page contains a set of tabs. All forms have the following tabs:
 *  - General Info ([[ItemFormMainTabComponent]]), which has the item's name, status, children, and various other information.
 *  - Usage, which has one or two tables:
 *    - one showing all the instances of that item which are currently running/logged on
 *    - all but [[UserMainTabComponent]] have a table showing what higher-level items have been assigned this one. Like a vm's table
 *      will show what virtues have been assigned that vm.
 *  - History (currently not implemented), which shows the history of changes made to this Item.
 *
 * Other tables are detailed in the comments on each subclass.
 *
 * The user can navigate to the view pages for all items linked to this [[item]], but only when the page is in view mode.
 *
 * @extends [[GenericDataPageComponent]] because it needs to load data about an Item and its children, as well as other available children
 */
@Component({
  providers: [ BaseUrlService, DataRequestService ]
})
export abstract class ItemFormComponent extends GenericTabbedFormComponent implements OnInit {

  /**
   * It appears that we can't really use the Angular Form tool/paradigm without extensive refactoring.
   * As is, we have variables for all the different attributes one could set, as well as for all the data
   * we wish to show the user, and can hopefully just make some of it editable/static depending on what mode
   * the form page is in (view/create/edit etc). With forms, the variables are attributes of the form item, and
   * accessed/updated/retrieved (apparently) only within the form construct. The html files would change entirely,
   * and you'd need either both a bunch of attributes and a form (for viewing/editing respectively), or only a form
   * which just isn't really a form when you're in view mode.
   * Having gotten this far, it seems most prudent to continue updating/collecting/describing the various fields manually.
   */
  // itemForm: FormGroup;

  /**
   * Holds the [[Item]] being viewed/created/edited/duplicated.
   *
   * Initialized empty, but given an ID on render ([[ngOnInit]]()). The ID is parsed out of the path,
   * and once all requested datasets are retrieved ([[onPullComplete]]()), item is overwritten with whatever Item
   * in [[datasetName]] has that ID.
   *
   * Note that in create mode, item.id is empty.
   *
   * New IDs for creation and duplication are generated server-side.
   *
   * The only difference between item in edit mode and duplicate mode, is whether the backend
   * generates a new ID for the object when saving it.
   */
  item: Item;

  /**
   * what the user is doing to the item: {CREATE, EDIT, DUPLICATE, VIEW}
   * Holds the strings 'Create', 'Edit', 'Duplicate', or 'View' resp., for display to the user
   */
  mode: Mode;

  /** holds the name of the relevant dataset for Item being viewed;
   *   e.g., in virtue.component, it should be set to `DatasetNames.VMS`
   * Must be set in constructor of derived class.
   * Can't hold direct reference because that reference won't be updated when
   * the dataset is pulled or re-pulled
   */
  datasetName: DatasetNames;

  /** holds the name of the relevant dataset for the children of the Item being viewed;
   *   e.g., in virtue.component, it should be set to `DatasetNames.VMS`
   * Must be set in constructor of derived class.
   * Can't hold direct reference because that reference won't be updated when
   * the dataset is pulled or re-pulled
   */
  childDatasetName: DatasetNames;

  /**
   * see [[GenericTabbedFormComponent]] and [[GenericPageComponent.constructor]] for notes on inherited parameters
   * @param parentDomain Used to check page for errors, and navigate back to the list page for this type of item upon save or cancel
   */
  constructor(
    protected parentDomain: string,
    location: Location,
    activatedRoute: ActivatedRoute,
    router: Router,
    baseUrlService: BaseUrlService,
    dataRequestService: DataRequestService,
    dialog: MatDialog
  ) {
    super(location, activatedRoute, router, baseUrlService, dataRequestService, dialog);

    // the mode needs to be set before any other work can be done
    this.setMode();
  }

  /**
   * This parses the url path (via the routing info in activatedRoute)
   * and uses that to set what mode (view/create/edit/duplicate) the page
   * ought to be in.
   *
   * This has a number of checks, because it would probably break if there are changes made to
   * the webapp's navigational structure.
   *
   * @return true if the mode could be determined, false on error.
   */
  setMode(): boolean {
    this.mode = Mode.CREATE;
    let urlValid = true;
    // Parse url, making sure it's set up the expected way.

    let url = this.router.routerState.snapshot.url;
    // console.log(this.location);
    // The url may start with an initial slash; remove it if it does.
    if (url[0] === '/') {
      url = url.substr(1);
    }
    // split the path into a list of route stops,
    // check that the first stop is the same as what the parentDomain for this component should be.
    // Remember that the parentDomain has a beginning forward slash, while the split URL had its slashes removed.
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
    } else if (route[1] === 'view') {
        this.mode = Mode.VIEW;
    } else {
        // something about the routing system has changed.
        urlValid = false;
    }
    if (!urlValid) {
      if (this.router.routerState.snapshot.url === this.parentDomain) {
        // apparently sometimes when an error happens on a form page, the system
        // quits and returns to {parentDomain}, and then for some reason re-calls the
        // constructor for the form component it just left. Which leads here and
        // breaks because the URL is wrong. Strange. So don't print out the below
        // error on those cases.
        return false;
      }
      console.log("ERROR: Can't decipher URL; Something about \
the routing system has changed. Returning to " + this.parentDomain.substr(1) + " page.\n       Expects something like \
" + this.parentDomain + "/create, " + this.parentDomain + "/duplicate/key-value, or " + this.parentDomain + "/edit/key-value,\
 but got: \n       " + this.router.routerState.snapshot.url);
      this.goToPage(this.parentDomain);
      return false;
    }
    return true;
  }

  /**
   * On render, store the id of the item to be viewed/edited/duplicated from the specified path.
   * This id is what's used to set [[item]] in [[onPullComplete]]().
   *
   * Then call the same functions that get called on render in all [[GenericPageComponent]]s.
   * @override parent [[GenericTabbedFormComponent.ngOnInit]]()
   */
  ngOnInit(): void {
    if (this.mode !== Mode.CREATE) {
      this.item.setID(this.activatedRoute.snapshot.params['id']);
    }

    this.cmnDataComponentSetup();
    this.initializeTabs();
  }

  /**
   * See comments at [[GenericPageComponent.onPullComplete]]()
   *
   * Set up the parts of the page/tabs that rely on this.item, and set up the page only the first time
   *
   * @see [[initialPullComplete]]
   */
  onPullComplete(): void {
    // Refreshes to the datasets should only update the actual datasets in the background, and not overwrite/undo
    // any changes/edits made to the data on the page.
    // So update [[item]] only the first time datasets are pulled, or if the page is only being viewed.
    if (! this.initialPullComplete || this.mode === Mode.VIEW) {
      if (this.mode !== Mode.CREATE) {// no data to retrieve if creating a new one.
        this.initItem();
      }
      this.setUpTabs();
    }

    this.updatePage();
    this.initialPullComplete = true;
  }

  /**
   * abstracts away what needs to happen when the page loads or reloads
   * Build item's children - take the current lists of IDs that item has, and build its attributes based
   * on the datasets that were pulled from the backend when the page loaded.
   */
  updatePage(): void {
    for (let childDatasetName of this.datasetsMeta[this.datasetName].depends) {
      this.buildIndexedObjAttribute(this.item, childDatasetName);
    }
    this.updateTabs();
  }

  /**
   * item.id must have been previously set.
   * Overwrites [[item]] with the Item from [[datasetName]] that has ID === item.id
   *
   * Note that this doesn't initialize item.children.
   */
  initItem(): void {
    let _item = this.datasets[this.datasetName].get(this.item.getID());
    if (_item) {
      this.item = _item;
    }
    else {
      console.log("No record with ID", this.item.getID(), "found in dataset", this.datasetName + ".");
      // TODO let the user know it didn't load, and probably remove this console log

      // return to parent list page without saving anything.
      this.toListPage();
    }
  }

  /**
   * Change over to edit mode. Does no saving or processing, just re-renders things
   * in the new mode. Doesn't
   * This now just changes the URL, re-sets [[mode]], and asks the tabs
   * to update.
   */
  toEditMode(): void {
    // this.editItem(this.item); // this reloads the whole page
    this.location.go(this.item.getPageRoute(Mode.EDIT));
    // this.setMode(); // this uses the router state, which isn't affected by location.*()
    this.mode = Mode.EDIT;
    this.updateTabs();

  }

  /**
   * Change over to view mode.
   *
   * Should only be called after saving the previously-entered data.
   *
   * This now just changes the URL, re-sets [[mode]], and re-pulls data
   *
   * It used to call this.viewItem(this.item), but that reloads the whole page.
   *
   * And note that mode has to be set explicitly - even though it looks like you could call
   * this.setMode() after changing the URL and have the mode be set correctly automatically,
   * setMode uses the router state and not the URL directly. Apparently the router state isn't
   * affected by any Location method.
   */
  toViewMode(): void {
    this.location.go(this.item.getPageRoute(Mode.VIEW));

    this.mode = Mode.VIEW;
    this.cmnDataComponentSetup();
  }

  /**
   * Return to this Item's list page. Does no processing or saving first.
   */
  toListPage(): void {
    this.goToPage(this.parentDomain);
  }

  /**
   * Save changes to backend and return to list page. Or should it be to previous domain?
   */
  saveAndReturn(): void {
    this.createOrUpdate(() => this.toListPage());
  }

  /**
   * save changes to backend, staying on current page (but switching to view mode)
   */
  save(): void {
    this.createOrUpdate(() => this.toViewMode());
  }

  /**
   * Check and save the item's current state, if valid.
   *  - (create/duplicate mode) saved as a new item
   *  - (edit mode) saved by overwriting whatever record on the backend has item's ID.
   * Note, that this means that creating a user (whose ID is their username) with the same username as
   * an existing user, will overwrite that existing user's data. There is a check for that in [[finalizeItem]]().
   *
   * @param redirect a redirect function to call (only) after the saving process has successfully completed.
   */
  private createOrUpdate(redirect: (newItemData?) => void): void {
    // collects/updates data for and in the item, in preparation for saving.
    if ( ! this.finalizeItem()) {
      console.log("Item not valid."); // TODO give useful error message
      return;
    }

    if ( this.inDuplicateMode() || this.inCreateMode() ) {
      this.createItem(this.item, redirect);
    }
    else if ( this.inEditMode() ) {
      this.updateItem(this.item, redirect);
    }
    else {
      console.log("Could not save or update - mode not valid. Mode set to: ", this.mode);
    }
  }

  /**
   * @return true iff the page is in view mode.
   * Used in the html to prevent some types of action when the page is in 'View' mode.
   */
  inViewMode(): boolean {
    return this.mode === Mode.VIEW;
  }

  /**
   * @return true iff the page is in EDIT mode.
   * Used in the html to prevent some actions while in EDIT mode.
   */
  inEditMode(): boolean {
    return this.mode === Mode.EDIT;
  }

  /**
   * @return true iff the page is in CREATE mode.
   * Used in the html to change how the page gets displayed in CREATE mode.
   */
  inCreateMode(): boolean {
    return this.mode === Mode.CREATE;
  }

  /**
   * @return true iff the page is in DUPLICATE mode.
   * Used in the html to change how the page gets displayed in DUPLICATE mode.
   */
  inDuplicateMode(): boolean {
    return this.mode === Mode.DUPLICATE;
  }


  /**
   * create and fill the fields the backend expects to see, pull in/record any
   * uncollected inputs, and check that the item is valid to be saved
   *
   * @return true if [[item]] is valid and can be saved to the backend, false otherwise.
   */
  abstract finalizeItem(): boolean;

}
