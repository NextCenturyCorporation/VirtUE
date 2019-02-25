import { Component, OnInit, NgModule, ViewEncapsulation, Renderer2 } from '@angular/core';

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

   enteredButton: boolean = false;
   isMatMenuOpen: boolean = false;
   prevButtonTrigger: any;

  /** The list of items to appear in the header, with their urls. */
  private navigation = [
    {label: 'Dashboard', link: '/dashboard'},
    {label: 'Settings', link: '/settings'},
    {label: 'Users', link: '/users'},
    {label: 'Instances',
      dropdownOptions: [
        {label: 'Virtue Instances', link: '/virtue-instances'},
        {label: 'VM Instances', link: '/vm-instances'},
      ]
    },
    {label: 'Templates',
      dropdownOptions: [
        {label: 'Virtue Templates', link: '/virtues'},
        {label: 'VM Templates', link: '/vm-templates'}
      ]
    },
    {label: 'Applications    ', link: '/applications'}
  ];
  constructor(
      public authService: AuthenticationService,
      private ren: Renderer2
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

  /**
   * Copied from stackoverflow - trust only with caution
   * https://stackoverflow.com/questions/53618333/how-to-open-and-close-angular-mat-menu-on-hover/53618962#53618962
   * Makes menu tabs open and close on mouse-over and mouse-leave.
   */

   menuenter() {
     this.isMatMenuOpen = true;
   }

   menuLeave(trigger, button) {
     setTimeout(() => {
       if (!this.enteredButton) {
         this.isMatMenuOpen = false;
         trigger.closeMenu();
         this.ren.removeClass(button['_elementRef'].nativeElement, 'cdk-focused');
         this.ren.removeClass(button['_elementRef'].nativeElement, 'cdk-program-focused');
       } else {
         this.isMatMenuOpen = false;
       }
     }, 200);
   }


   buttonEnter(trigger) {
     setTimeout(() => {
       if (this.prevButtonTrigger && this.prevButtonTrigger !== trigger) {
         this.prevButtonTrigger.closeMenu();
         this.prevButtonTrigger = trigger;
         trigger.openMenu();
       }
       else if (!this.isMatMenuOpen) {
         this.enteredButton = true;
         this.prevButtonTrigger = trigger;
         trigger.openMenu();
       }
       else {
         this.enteredButton = true;
         this.prevButtonTrigger = trigger;
       }
     });
   }

   buttonLeave(trigger, button) {
     setTimeout(() => {
       if (this.enteredButton && !this.isMatMenuOpen) {
         trigger.closeMenu();
         this.ren.removeClass(button['_elementRef'].nativeElement, 'cdk-focused');
         this.ren.removeClass(button['_elementRef'].nativeElement, 'cdk-program-focused');
       } if (!this.isMatMenuOpen) {
         trigger.closeMenu();
         this.ren.removeClass(button['_elementRef'].nativeElement, 'cdk-focused');
         this.ren.removeClass(button['_elementRef'].nativeElement, 'cdk-program-focused');
       } else {
         this.enteredButton = false;
       }
     }, 100);
   }
}
