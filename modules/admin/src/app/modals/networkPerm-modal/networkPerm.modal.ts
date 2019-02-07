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

  getNetPerm = new EventEmitter();

  netPerm: NetworkPermission = new NetworkPermission();

  /**
   * see [[GenericPageComponent.constructor]] for notes on inherited parameters
   *
   * @param dialogRef injected, is a reference to the modal dialog box itself.
   */
  constructor(
      routerService: RouterService,
      dialog: MatDialog,

      /** injected, is a reference to the modal dialog box itself. */
      public dialogRef: MatDialogRef<NetworkPermissionModalComponent>
    ) {
      super(routerService, dialog);
      this.netPerm = new NetworkPermission();
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
