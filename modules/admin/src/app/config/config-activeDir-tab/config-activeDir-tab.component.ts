import { Component, OnInit } from '@angular/core';

import { MatDialog } from '@angular/material';
import { ActivatedRoute, Router } from '@angular/router';

import { GenericTabComponent } from '../../shared/abstracts/gen-tab/gen-tab.component';

/**
 * @class
 * This component allows the user (an admin) to set up activ directories. For something.
 * TODO ask about active directories
 * #uncommented, because this is a stub.
 * also #move to a tab on the global settings form
 */
@Component({
  selector: 'app-config-active-dir-tab',
  templateUrl: './config-activeDir-tab.component.html',
  styleUrls: ['./config-activeDir-tab.component.css']
})
export class ConfigActiveDirTabComponent extends GenericTabComponent implements OnInit {

  /**
   * see [[GenericPageComponent.constructor]] for notes on inherited parameters
   */
  constructor(
    router: Router,
    dialog: MatDialog
  ) {
    super(router, dialog);
    this.tabName = "Active Directories";
  }
  /**
   * #unimplemented
   */
  init(): void {
  }

  /**
   * #unimplemented
   */
  setUp(): void {
  }

  /**
   * #unimplemented
   */
  update(): void {
  }

  /**
   * #unimplemented
   */
  collectData(): boolean {
    return true;
  }
}
