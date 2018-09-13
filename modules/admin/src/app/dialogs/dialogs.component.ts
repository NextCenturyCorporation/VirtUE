import { Component, EventEmitter, OnInit, Inject, Output } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA  } from '@angular/material';
import { FormGroup } from '@angular/forms';

import { Item } from '../shared/models/item.model';

/**
 * @class This serves to check some irreversable user actions, to lessen the chance of them being made in error.
 * @example
 * usage:
 *
 *      openDialog(action: string, target: Item): void {
 *        let dialogRef = this.dialog.open(DialogsComponent, {
 *          width: '450px',
 *          data:  {
 *            some: data
 *          }
 *        });
 *
 *      //  control goes here after either "Ok" or "Cancel" are clicked on the dialog
 *      let sub = dialogRef.componentInstance.getResponse.subscribe((shouldProceed) => {
 *        console.log(shouldProceed);
 *        if (shouldProceed) {
 *          performSomeAction();
 *        }
 *      },
 *      ()=>{
 *        sub.unsubscribe();
 *      },// on error
 *      ()=>{  // on non-error
 *        sub.unsubscribe(); // stop listening for a response; it only comes once.
 *      });
 *    }
 */
@Component({
  selector: 'app-dialogs',
  templateUrl: './dialogs.component.html'
})
export class DialogsComponent {

  /** what's being done: e.g. 'delete', 'disable' */
  actionType: string;

  /** the name of the item the user wants to do something to */
  targetName: string;

  /** What the component that created this dialog watches, to know what the user clicked. true for submit, false for cancel*/
  @Output() getResponse: EventEmitter<boolean> = new EventEmitter();

  /**
   *  This is a dialog for confirming an action, when reversing it may be inconvenient.
   *  Currently I think it's just used when deleting virtues/users/vms, and disabling the admin user account.
   */
  constructor(
    /** The actual dialog box itself. */
    public dialogRef: MatDialogRef<DialogsComponent>,
    /** input from the calling component */
    @Inject( MAT_DIALOG_DATA ) public data: {
      /** the action that's being checked - see comment on [[DialogsComponent.actionType]] */
      actionType: string,
      /** What to call the object to which the action would be applied */
      targetName: string
    }
  ) {
    // The following are only used on the dialog html page.
    this.actionType = this.data.actionType;
    this.targetName = this.data.targetName;
  }

  /**
   * Called when the user clicks submit, sends 'true' back
   * to the component that spawned this dialog, and closes the dialog.
   */
  submit(): void {
    this.getResponse.emit(true);
    this.dialogRef.close();
  }

  /**
   * sends false back to the component that spawned this, before closing itself.
   */
  cancel(): void {
    this.getResponse.emit(false);
    this.dialogRef.close();
  }
}
