import { Component, OnInit, NgModule } from '@angular/core';

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.css']
})
export class HeaderComponent implements OnInit {
  showDropDown:boolean = false;
  navigation = [
    {value: 'Dashboard', link: '/dashboard'},
    {value: 'Settings', link: '/settings'},
    {value: 'Users', link: '/users'},
    {value: 'Applications', link: '/applications'},
    // {value: 'Templates', link: '/'}
    {value: 'Virtual Machines', link: '/vm-templates'},
    {value: 'Virtues', link: '/virtues'}
  ];
  templateLinks = [
    {value: 'Virtual Machines', link: '/vm-templates'},
    {value: 'Virtues', link: '/virtues'}
  ]

  ngOnInit() {

  }

  getValue(n) {
    console.log(n);
    console.log(n.value);
    return n.value;
  }

  toggleCollapse() {
    this.showDropDown = !this.showDropDown;
  }

  getDropDown() {
    console.log(this.showDropDown);
    return this.showDropDown;
  }

}
