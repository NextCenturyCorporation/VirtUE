import { Component, OnInit, NgModule,ViewEncapsulation } from '@angular/core';

import { AuthenticationService } from '../shared/services/authentication.service';

/**
 * @class
 * This class represents the header which is displayed on every page, allowing
 * the navigation between the main pages of the application.
 *
 * Eventually changing this around somehow would be good - perhaps a "virtual machine" and "virtue" dropdown, each with links to
 * the Templates and the Instances.
 *
 * A tab for records of administrator actions would also be useful.
 *
 * The dashboard is currently the webapp's landing page.
 */
@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.css']
})
export class HeaderComponent implements OnInit {

  /** The list of items to appear in the header, with their urls. */
  private navigation = [
    {value: 'Dashboard', link: '/dashboard'},
    {value: 'Settings', link: '/settings'},
    {value: 'Users', link: '/users'},
    {value: 'Running Virtues', link: '/virtue-instances'},
    {value: 'Running Vms', link: '/vm-instances'},
    // {value: 'Running Instances',
    //   dropdownOptions: [
    //     {value: 'Running Virtues', link: '/virtue-instances'},
    //     {value: 'Running Vms', link: '/vm-instances'},
    //   ]
    // },
    // {value: 'Templates',
    //   dropdownOptions: [
    //     {value: 'Virtue Templates', link: '/virtues'},
    //     {value: 'Virtual Machine Templates', link: '/vm-templates'}
    //   ]
    // },
    {value: 'Virtue Templates', link: '/virtues'},
    {value: 'Virtual Machine Templates', link: '/vm-templates'},
    {value: 'Applications', link: '/applications'}
  ];

  constructor(
      public authService: AuthenticationService
    ) {

  }

  /**
   * Do nothing extra on render at the moment.
   */
  ngOnInit() {
  }

  getNavOptions() {
    if (this.authService.isAuthenticated()) {
      return this.navigation;
    }
    return [];
  }
}
