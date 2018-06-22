import { Component, OnInit } from '@angular/core';

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
    {value: 'Applications', link: '/applications'},
    {value: 'Virtual Machines', link: '/virtual-machines'},
    {value: 'Virtues', link: '/virtues'}
  ];

  ngOnInit() {}

}
