import { Component, OnInit, ViewChild, ViewChildren, QueryList } from '@angular/core';
import { HttpEvent, HttpHandler, HttpRequest } from '@angular/common/http';
import { Location } from '@angular/common';
import { FormGroup, FormBuilder } from '@angular/forms';
import { MatDialog } from '@angular/material';
import { MatTabsModule } from '@angular/material/tabs';
import { ActivatedRoute, Router } from '@angular/router';
import { Observable } from 'rxjs/Observable';

import { BaseUrlService } from '../../services/baseUrl.service';
import { ItemService } from '../../services/item.service';

import { DialogsComponent } from '../../../dialogs/dialogs.component';

import { Item } from '../../models/item.model';
import { DictList } from '../../models/dictionary.model';
import { RowOptions } from '../../models/rowOptions.model';
import { Column } from '../../models/column.model';
import { Mode, Datasets } from '../../enums/enums';

import { GenericPageComponent } from '../gen-page/gen-page.component';
import { GenericFormTabComponent } from '../gen-tab/gen-tab.component';
import { GenericTableComponent } from '../gen-table/gen-table.component';
import { GenericModalComponent } from '../../../modals/generic-modal/generic.modal';
import { VirtueModalComponent } from '../../../modals/virtue-modal/virtue-modal.component';
import { VmModalComponent } from '../../../modals/vm-modal/vm-modal.component';

/**
 * #uncommented
 * @class
 * @extends
 */
@Component({
  providers: [ BaseUrlService, ItemService ]
})
export abstract class GenericFormComponent extends GenericPageComponent implements OnInit {

  // It appears that we can't really use the Angular Form tool/paradigm without extensive refactoring.
  // As is, we have variables for all the different attributes one could set, as well as for all the data
  // we wish to show the user, and can hopefully just make some of it editable/static depending on what mode
  // the form page is in (view/create/edit etc). With forms, the variables are attributes of the form item, and
  // accessed/updated/retrieved (apparently) only within the form construct. The html files would change entirely,
  // and you'd need either both a bunch of attributes and a form (for viewing/editing respectively), or only a form
  // which just isn't really a form when you're in view mode.
  // Having gotten this far, it seems most prudent to continue updating/collecting/describing the various fields manually.
  // itemForm: FormGroup;

  itemName: string;

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

  // top-domain for child type. So for user.component, this would be '/virtues'
  childDomain: string;

  // holds the name of the relevant dataset for the class;
  //   i.e., in virtue.component, it should be set to 'allVms'
  // Must be set in constructor of derived class.
  // Can't hold direct link because that reference won't be updated when
  // the dataset is pulled or re-pulled
  datasetName: string;
  childDatasetName: string;

  /**
   * @param
   *
   * @return
   */
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

  }

  /**
   * @param
   *
   * @return
   */
  // This checks the current routing info (the end of the current url)
  // and uses that to set what mode (create/edit/duplicate) the page
  //  ought to be in.
  setMode() {
    this.mode = Mode.CREATE;
    let urlValid = true;
    // Parse url, making sure it's set up the expected way.

    let url = this.router.routerState.snapshot.url;
    if (url[0] === '/') {
      url = url.substr(1);
    }
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

  /**
   * @param
   *
   * @return
   */
  ngOnInit() {
    if (this.mode !== Mode.CREATE) {
      this.item.id = this.activatedRoute.snapshot.params['id'];
    }

    this.cmnComponentSetup();
    this.initializeTabs();
  }

  /**
   * @param
   *
   * @return
   */
  // overrides parent
  onPullComplete() {
    if (this.mode !== Mode.CREATE) {// no data to load if creating a new one.
      this.buildItem();
    }
    this.setUpTabs();
    this.updateTabs();
  }

  /**
   * @param
   *
   * @return
   */
  // called in parent's ngOnInit
  abstract initializeTabs(): void;

  /**
   * @param
   *
   * @return
   */
  // called in parent's onPullComplete
  abstract setUpTabs(): void;

  /**
   * @param
   *
   * @return
   */
  // called whenever item's child list is set or changes
  abstract updateTabs(): void;

  /**
   * @param
   *
   * @return
   */
  // abstracts away what needs to happen when the page loads
  // Most pages will at least build item.children
  abstract updatePage(): void;


  /**
   * @param
   *
   * @return
   */
  buildItem() {
    let _item = this[this.datasetName].get(this.item.id);
    if (_item) {
      this.item = _item;
      this.updatePage();
    }
    else {
      console.log("No item with ID", this.item.id, "found in dataset", this.datasetName + ".");
      // TODO let the user know it didn't load
      this.cancel();
    }
  }

  /**
   * @param
   *
   * @return
   */
  setModeEdit() {
    this.mode = Mode.EDIT;
    this.updateTabs();
  }

  /**
   * @param
   *
   * @return
   */
  setModeView() {
    this.mode = Mode.VIEW;
    this.updateTabs();
  }

  /**
   * @param
   *
   * @return
   *
   * Save changes to backend and return to list page. Or should it be to previous domain?
   */
  save() {
    this.createOrUpdate(true);
  }

  /**
   * @param
   *
   * @return
   *
   * save changes to backend, staying on current page (but switching to view mode)
   */
  apply() {
    this.createOrUpdate(false);
    // TODO: Can this be changed to just reset something? As opposed to loading
    // the whole page again?
    this.router.navigate([this.item.getPageRoute(Mode.VIEW)]);
  }

  /**
   * @param
   *
   * @return
   */
  private createOrUpdate(redirect: boolean) {
    // collects/updates data for and in the item, in preparation for saving.
    if ( ! this.finalizeItem()) {
      console.log("Item not valid."); // TODO give useful error message
      return;
    }
    console.log(this.item);
    if (this.mode === Mode.DUPLICATE || this.mode === Mode.CREATE) {
      this.createItem(redirect);
    }
    // See note in virtue.component, near end of template definition - need to be able to save even while in view mode.
    // else if ( this.mode === Mode.EDIT) {
    //   this.updateItem(redirect);
    // }
    else if ( this.mode === Mode.EDIT || this.mode === Mode.VIEW) {
      this.updateItem(redirect);
    }
    else {
      console.log("Could not save or update - mode not valid. Mode set to: ", this.mode);
    }
  }

  /**
   * @param
   *
   * @return
   */
  toggleItemStatus() {
    this.item.enabled = !this.item.enabled;
  }

  /**
   * @param
   *
   * @return
   */
  cancel() {
    this.router.navigate([this.parentDomain]);
  }

  /**
   * @param
   *
   * @return
   */
  // saves your edits to the backend
  updateItem(redirect: boolean): void {
    let sub = this.itemService.updateItem(this.serviceConfigUrl, this.item.getID(), JSON.stringify(this.item)).subscribe(
      data => {
        if (redirect) {
          this.router.navigate([this.parentDomain]);
        }
      },
      error => {
        console.log(error);
      },
      () => {// when finished
        sub.unsubscribe();
      });
  }

  /**
   * @param
   *
   * @return
   */
  // saves the selected settings as a new item
  createItem(redirect: boolean) {
    let sub = this.itemService.createItem(this.serviceConfigUrl, JSON.stringify(this.item)).subscribe(
      data => {
        if (redirect) {
          this.router.navigate([this.parentDomain]);
        }
      },
      error => {
        console.log(error.message);
      },
      () => {// when finished
        sub.unsubscribe();
      });
  }

  /**
   * @param
   *
   * @return
   */
  // overridden by virtue component
  getTableWidth(): number {
    return 9;
  }

  /**
   * @param
   *
   * @return
   */
  hasColoredLabels(): boolean {
    return false;
  }

  /**
   * @param
   *
   * @return
   */
  // create and fill the fields the backend expects to see, record any
  // uncollected inputs, and check that the item is valid to be saved
  abstract finalizeItem(): boolean;

  /**
   * @param
   *
   * @return
   */
  // can be overridden, if anything needs to be done manually upon item load.
  // currently overridden in virtue
  updateUnconnectedFields(): void {}


}
