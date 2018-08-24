import { Component, EventEmitter, OnInit, Inject, Output } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA  } from '@angular/material';
import { FormGroup } from '@angular/forms';

import { Item } from '../shared/models/item.model';

@Component({
  selector: 'app-dialogs',
  templateUrl: './dialogs.component.html'
})
export class DialogsComponent implements OnInit {

  form: FormGroup;
  actionType: string;
  targetName: string;
  targetId: string;
  @Output() dialogEmitter: EventEmitter<any> = new EventEmitter();

  /**
    This is a dialog for confirming an action, when reversing it may be inconvenient.
    Currently I think it's just used when deleting virtues/users/vms, and disabling the admin user account.
  */
  constructor(
    public dialogRef: MatDialogRef<DialogsComponent>,
    @Inject( MAT_DIALOG_DATA ) public data: {actionType: string, targetObject: Item}
  ) {
    // The folllowing are only used on the dialog html page.

    // actionType is what's being done: e.g. 'delete', 'disable'
    this.actionType = this.data.actionType;

    this.targetName = this.data.targetObject.getName();
  }

  ngOnInit() {
  }

  submit() {
    this.dialogEmitter.emit(this.data.targetObject);
    this.dialogRef.close();
  }

  cancel() {
    this.dialogEmitter.emit(0);
    this.dialogRef.close();
  }
}
