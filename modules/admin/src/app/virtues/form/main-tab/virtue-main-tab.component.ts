import { Component, OnInit, ViewChild, Output, EventEmitter } from '@angular/core';
import { HttpEvent, HttpHandler, HttpRequest } from '@angular/common/http';
import { Location } from '@angular/common';
import { FormControl } from '@angular/forms';
import { MatDialog } from '@angular/material';
import { MatTabsModule } from '@angular/material/tabs';
import { ActivatedRoute, Router } from '@angular/router';
import { Observable } from 'rxjs/Observable';

import { BaseUrlService } from '../../../shared/services/baseUrl.service';
import { ItemService } from '../../../shared/services/item.service';

import { DialogsComponent } from '../../../dialogs/dialogs.component';

import { Item } from '../../../shared/models/item.model';
import { User } from '../../../shared/models/user.model';
import { VirtualMachine } from '../../../shared/models/vm.model';
import { Virtue } from '../../../shared/models/virtue.model';
import { DictList } from '../../../shared/models/dictionary.model';
import { Column } from '../../../shared/models/column.model';
import { Mode, ConfigUrlEnum } from '../../../shared/enums/enums';
import { RowOptions } from '../../../shared/models/rowOptions.model';

import { VmModalComponent } from '../../../modals/vm-modal/vm-modal.component';

import { GenericTableComponent } from '../../../shared/abstracts/gen-table/gen-table.component';
import { GenericFormTab } from '../../../shared/abstracts/gen-tab/gen-tab.component';
// import { GenericModalComponent } from '../../../modals/generic-modal/generic.modal';

@Component({
  selector: 'app-virtue-main-tab',
  templateUrl: './virtue-main-tab.component.html',
  styleUrls: ['../../../shared/abstracts/gen-list/gen-list.component.css']
})

export class VirtueMainTabComponent extends GenericFormTab implements OnInit {

  @ViewChild('childrenTable') childrenTable: GenericTableComponent;

  // emits a list of childID strings.
  @Output() onChildrenChange: EventEmitter<string[]> = new EventEmitter<string[]>();

  newVersion: number;

  constructor(router: Router, dialog: MatDialog) {
    super(router, dialog);
    this.tabName = "General Info";

  }

  update() {
    this.childrenTable.items = this.item.children.asList();
  }

  setUp(mode: Mode, item: Item): void {
    this.mode = mode;
    this.item = item;
    this.newVersion = Number(this.item['version']);

    if (this.mode !== Mode.VIEW && this.mode !== Mode.CREATE) {
      this.newVersion++;
    }

  }

  getColumns(): Column[] {
    return [
      new Column('name',            'Template Name',        false, 'asc',     4, undefined, (i: Item) => this.viewItem(i)),
      new Column('os',              'OS',                   false, 'asc',     2),
      new Column('childNamesHTML',  'Assigned Applications', true, undefined, 4, this.getChildNamesHtml),
      new Column('status',          'Status',               false, 'asc',     2, this.formatStatus)
    ];
  }

  getOptionsList(): RowOptions[] {
    return [
       new RowOptions("Edit", () => true, (i:Item) => this.viewItem(i)),
       new RowOptions("Remove", () => true, (i: Item) => this.openDialog('delete', i))
    ];
  }

  getNoDataMsg(): string {
    return "No virtual machine templates have been added yet. To add a virtual machine template, click on the button \"Add VM\" above.";
  }

  getPageOptions(): {
      serviceConfigUrl: ConfigUrlEnum,
      neededDatasets: string[]} {
    return {
      serviceConfigUrl: ConfigUrlEnum.USERS,
      neededDatasets: ["apps", "vms", "virtues", "users"]
    };
  }

  init() {
    this.setUpChildTable();
  }

  setUpChildTable(): void {
    if (this.childrenTable === undefined) {
      return;
    }

    this.childrenTable.setUp({
      cols: this.getColumns(),
      opts: this.getOptionsList(),
      coloredLabels: true,
      filters: [], // don't allow filtering on the form's child table. ?
      tableWidth: 9,
      noDataMsg: this.getNoDataMsg(),
      hasCheckBoxes: false
    });
  }

  collectData() {
    this.item['version'] = this.newVersion;
  }

  /**
   this is a checker, if the user clicks 'remove' on one of the item's children.
   Could be improved/made more clear/distinguished from the "activateModal" method.
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
  // getModal(
  //   params: {width: string, height: string, data: {id: string, selectedIDs: string[] }}
  // ): any {}

  getModal(
    params: {width: string, height: string, data: {id: string, selectedIDs: string[] }}
  ): any {
    return this.dialog.open( VmModalComponent, params);
  }


  // this brings up the modal to add/remove children
  // this could be refactored into a "MainTab" class, which is the same for all
  // forms, but I'm not sure that'd be necessary.
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
      this.onChildrenChange.emit(selectedVirtues);
    },
    () => {},
    () => {// when finished
      sub.unsubscribe();
    });
    let leftPosition = ((window.screen.width) - dialogWidth) / 2;

    dialogRef.updatePosition({ top: '5%', left: leftPosition + 'px' });

  }

}
