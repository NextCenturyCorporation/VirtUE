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

  _addItem = new Subject<Breadcrumb>();

  constructor(private router: Router) { }

  addItem(label: string, href: string = this.router.url): void {
    this._addItem.next(new Breadcrumb(label, href));
  }

}
