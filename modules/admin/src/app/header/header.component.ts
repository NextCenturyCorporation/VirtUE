import { Component, OnInit, NgModule } from '@angular/core';

/**
 * #uncommented
 * @class
 */
@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.css']
})
export class HeaderComponent implements OnInit {

  navigation = [
    {value: 'Dashboard', link: '/dashboard'},
    {value: 'Settings', link: '/settings'},
    {value: 'Users', link: '/users'},
    {value: 'Virtue Templates', link: '/virtues'},
    {value: 'Virtual Machine Templates', link: '/vm-templates'},
    {value: 'Applications', link: '/applications'}
  ];

  ngOnInit() {
  }
}
