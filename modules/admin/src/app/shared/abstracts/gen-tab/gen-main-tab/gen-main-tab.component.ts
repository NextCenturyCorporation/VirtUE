import { Component, OnInit, ViewChild, Output, EventEmitter } from '@angular/core';
import { MatDialog } from '@angular/material';
import { ActivatedRoute, Router } from '@angular/router';

import { DialogsComponent } from '../../../../dialogs/dialogs.component';

import { Item } from '../../../models/item.model';

import { GenericTableComponent } from '../../gen-table/gen-table.component';
import { GenericFormTabComponent } from '../../gen-tab/gen-tab.component';


export abstract class GenericMainTabComponent extends GenericFormTabComponent implements OnInit {

  @ViewChild('childrenTable') protected childrenTable: GenericTableComponent;

  // to notify user.component that a new set of childIDs have been selected
  @Output() onChildrenChange: EventEmitter<string[]> = new EventEmitter<string[]>();

  constructor(router: Router, dialog: MatDialog) {
    super(router, dialog);
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

  // this brings up the modal to add/remove children
  // this could be refactored into a "MainTab" class, which is the same for all
  // forms, but I'm not sure that'd be necessary.
  activateModal(mode: string): void {
    let dialogHeight = 600;
    let dialogWidth = 800;

    let params = {
      height: dialogHeight + 'px',
      width: dialogWidth + 'px',
      data: {
        id: this.item.getName(),
        selectedIDs: this.item.childIDs
      }
    };

    let dialogRef = this.getDialogRef(params);

    let sub = dialogRef.componentInstance.getSelections.subscribe((selectedItems) => {
      this.onChildrenChange.emit(selectedItems);
    },
    () => {},
    () => {// when finished
      sub.unsubscribe();
    });
    let leftPosition = ((window.screen.width) - dialogWidth) / 2;

    dialogRef.updatePosition({ top: '5%', left: leftPosition + 'px' });

  }

  // is class-specific, which modal is called is determined by main-tab subclass.
  abstract getDialogRef(params: {height: string, width: string, data: any});

}
