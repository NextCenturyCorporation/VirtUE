import { Component, OnInit, ViewChild } from '@angular/core';
import { HttpEvent, HttpHandler, HttpRequest } from '@angular/common/http';
import { Location } from '@angular/common';
import { FormControl } from '@angular/forms';
import { MatDialog } from '@angular/material';
import { MatTabsModule } from '@angular/material/tabs';
import { ActivatedRoute, Router } from '@angular/router';
import { Observable } from 'rxjs/Observable';

import { BaseUrlService } from '../../shared/services/baseUrl.service';
import { ItemService } from '../../shared/services/item.service';

import { DialogsComponent } from '../../dialogs/dialogs.component';

import { VirtueModalComponent } from '../../modals/virtue-modal/virtue-modal.component';

import { Item } from '../../shared/models/item.model';
import { User } from '../../shared/models/user.model';
import { VirtualMachine } from '../../shared/models/vm.model';
import { Virtue } from '../../shared/models/virtue.model';
import { DictList } from '../../shared/models/dictionary.model';
import { Column } from '../../shared/models/column.model';
import { Mode, ConfigUrlEnum } from '../../shared/enums/enums';
import { RowOptions } from '../../shared/models/rowOptions.model';

import { GenericTableComponent } from '../../shared/abstracts/gen-table/gen-table.component';
import { GenericFormTab } from '../../shared/abstracts/gen-tab/gen-tab.component';
// import { GenericModalComponent } from '../../../modals/generic-modal/generic.modal';

@Component({
  selector: 'app-main-user-tab',
  templateUrl: './main-user-tab.component.html',
  styleUrls: ['../../shared/abstracts/gen-list/gen-list.component.css']
})

export class UserMainTabComponent extends GenericFormTab implements OnInit {

  @ViewChild('childrenTable') childrenTable: GenericTableComponent;

  roleUser: boolean;
  roleAdmin: boolean;

  fullImagePath: string;

  childDatasetName: string;

  constructor(router: Router, dialog: MatDialog) {
    super(router, dialog);
    this.tabName = "General Info";

    this.childDatasetName = 'allVirtues';
  }


  setUp(mode: Mode, item: Item): void {

    this.mode = mode;
    this.item = item;
    this.childrenTable.items = this.item.children.asList();




    this.roleUser = this.item['roles'].includes("ROLE_USER");
    this.roleAdmin = this.item['roles'].includes("ROLE_ADMIN");
  }

  tearDown(): any {

  }

  getColumns(): Column[] {
    return [
      // See note in gen-form getOptionsList
      new Column('name',            'Template Name',    false, 'asc',     3, undefined, (i: Item) => this.viewItem(i)),
      // new Column('name',            'Template Name',      false, 'asc',     2),
      new Column('childNamesHTML',  'Virtual Machines', true, undefined,  3, this.getChildNamesHtml),
      new Column('apps',            'Applications',     true, undefined,  3, this.getGrandchildrenHtmlList),
      new Column('version',         'Version',          false, 'asc',     2),
      new Column('status',          'Status',           false, 'asc',     1, this.formatStatus)
    ];
  }

  viewItem(i: Item) {
    if (i.getDomain()) {
      this.router.navigate([i.getDomain()]);
    }
  }

  getNoDataMsg(): string {
    return "No users have been created yet. To add a user, click on the button \"Add User\" above.";
  }

  getPageOptions(): {
      serviceConfigUrl: ConfigUrlEnum,
      neededDatasets: string[]} {
    return {
      serviceConfigUrl: ConfigUrlEnum.USERS,
      neededDatasets: ["apps", "vms", "virtues", "users"]
    };
  }

  // create and fill the fields the backend expects to see, record any
  // uncollected inputs, and check that the item is valid to be saved
  finalizeItem(): boolean {
    // TODO perform checks here, so none of the below changes happen if the item
    // isn't valid

    // remember these aren't security checks, merely checks to prevent the user
    // from accidentally putting in bad data

    //  remember to check enabled

    this.item['roles'] = [];
    if (this.roleUser) {
      this.item['roles'].push('ROLE_USER');
    }
    if (this.roleAdmin) {
      this.item['roles'].push('ROLE_ADMIN');
    }

    if (this.mode === Mode.CREATE && !this.item['username']) {
      return confirm("You need to enter a username.");
    }

    // if not editing, make sure username isn't taken

    this.item['username'] = this.item.name,
    this.item['authorities'] = this.item['roles'], // since this is technically an item
    this.item['virtueTemplateIds'] = this.item.childIDs;

    // so we're not trying to stringify a bunch of extra fields and data
    this.item.children = undefined;
    this.item.childIDs = undefined;
    this.item['roles'] = undefined;
    return true;
  }

  // overrides parent
  // remember this is for the table, holding the user's virtues
  hasColoredLabels() {
    return true;
  }


  buildChildTable(): void {
    if (this.childrenTable === undefined) {
      return;
    }

    this.childrenTable.setUp({
      cols: this.getColumns(),
      opts: this.getOptionsList(),
      coloredLabels: this.hasColoredLabels(),
      filters: [], // don't allow filtering on the form's child table. ?
      tableWidth: 9,
      noDataMsg: this.getNoDataMsg(),
      hasCheckBoxes: false
    });
  }


  // overrides parent
  getOptionsList(): RowOptions[] {
    return [
       new RowOptions("Edit", () => true, (i:Item) => this.viewItem(i)),
       new RowOptions("Remove", () => true, (i: Item) => this.openDialog('delete', i))
    ];
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
          //  this.setItemStatus(targetObject, false);
          console.log(targetObject);
          this.item.removeChild(targetObject.getID());

          // remove from childIDs and children
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
  // getModal(
  //   params: {width: string, height: string, data: {id: string, selectedIDs: string[] }}
  // ): any {}

  getModal(
    params: {width: string, height: string, data: {id: string, selectedIDs: string[] }}
  ): any {
    return this.dialog.open( VirtueModalComponent, params);
  }


  // if nothing is passed in, we just want to populate item.children
  updateChildList( newVmIDs?: string[] ) {
  //TODO TODO
  // this doesn't know about databases though. Can it pass the new children to the parent?
    if (newVmIDs instanceof Array) {
      this.item.childIDs = newVmIDs;
    }

    // item was passed as a reference so this should update the other tabs as well.
    this.item.buildChildren(this[this.childDatasetName]);
    this.childrenTable.items = this.item.children.asList();
  }

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

}
