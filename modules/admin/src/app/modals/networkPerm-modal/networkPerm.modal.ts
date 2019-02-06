import { Component, EventEmitter, Inject, Input, OnInit, ViewChild } from '@angular/core';
import { FormGroup } from '@angular/forms';
import { MatDialog } from '@angular/material';

import { RouterService } from '../../shared/services/router.service';

import { GenericPageComponent } from '../../shared/abstracts/gen-page/gen-page.component';

import { IndexedObj } from '../../shared/models/indexedObj.model';
import { NetworkPermission } from '../../shared/models/networkPerm.model';
import { NetworkProtocols } from '../../virtues/protocols.enum';

import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material';


@Component({
  selector: 'app-network-permission-modal',
  templateUrl: './networkPerm.modal.html',
  styleUrls: ['./networkPerm.modal.css']
})
export class NetworkPermissionModalComponent extends GenericPageComponent implements OnInit {

  /** What the containing component watches, to get the user's selections back out of this modal. */
  getNetPerm = new EventEmitter();

  netPerm: NetworkPermission = new NetworkPermission();

  /**
   * see [[GenericPageComponent.constructor]] for notes on inherited parameters
   *
   * @param dialogRef injected, is a reference to the modal dialog box itself.
   * @param data is defined in calling component, holds the initial selections
   */
  constructor(
      routerService: RouterService,
      dialog: MatDialog,

      /** injected, is a reference to the modal dialog box itself. */
      public dialogRef: MatDialogRef<NetworkPermissionModalComponent>,

      /** holds the initial selections, and possibly a SelectionMode */
      @Inject(MAT_DIALOG_DATA) public data: any
    ) {
      super(routerService, dialog);

      if (data && data['templateID']) {
        this.netPerm = new NetworkPermission(data);
      }
      else {
        console.log("No field 'templateID' in data input to modal");
        this.dialogRef.close();
      }

  }

  ngOnInit(): void {

  }

  getProtocols(): string[] {
    return Object.values(NetworkProtocols);
  }

  submit(): void {
    if (!this.netPerm.checkValid()) {
      return;
    }
    this.getNetPerm.emit(this.netPerm);
    this.dialogRef.close();
  }

  cancel(): void {
    this.dialogRef.close();
  }
}
