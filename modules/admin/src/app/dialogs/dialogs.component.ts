import { Component, EventEmitter, OnInit, Inject, Output } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA  } from '@angular/material';
import { FormGroup } from '@angular/forms';

import { Item } from '../shared/models/item.model';

@Component({
  selector: 'app-dialogs',
  templateUrl: './dialogs.component.html'
})
export class DialogsComponent {

  /** what's being done: e.g. 'delete', 'disable' */
  actionType: string;
  targetName: string;

  /** What the component that created this dialog watches, to know what the user clicked. true for submit, false for cancel*/
  @Output() getResponse: EventEmitter<boolean> = new EventEmitter();

  /**
    This is a dialog for confirming an action, when reversing it may be inconvenient.
    Currently I think it's just used when deleting virtues/users/vms, and disabling the admin user account.
  */
  constructor(
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

    // actionType is what's being done: e.g. 'delete', 'disable'
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
