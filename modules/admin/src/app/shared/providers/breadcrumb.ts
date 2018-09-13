import { Injectable } from '@angular/core';
import { Router } from '@angular/router';

import { Subject } from 'rxjs';

import { Breadcrumb } from '../models/breadcrumb.model';

/**
 * #uncommented
 * @class
 */
@Injectable()
export class BreadcrumbProvider {

  /** #uncommented */
  _addItem = new Subject<Breadcrumb>();

  /**
   * #uncommented
   * @param
   *
   * @return
   */
  constructor(private router: Router) { }

  /**
   * #uncommented
   * @param
   *
   * @return
   */
  addItem(label: string, href: string = this.router.url): void {
    this._addItem.next(new Breadcrumb(label, href));
  }

}
