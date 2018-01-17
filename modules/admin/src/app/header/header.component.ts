import { Component, OnInit } from '@angular/core';
import { MatToolbarModule } from '@angular/material';

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.css']
})
export class HeaderComponent implements OnInit {

  navigation = [
    {value: 'Dashboard', link: '/dashboard'},
    {value: 'Settings', link: '/config'},
    {value: 'Users', link: '/users'},
    {value: 'Virtual Machines', link: '/virtual-machines'}
    {value: 'Virtues', link: '/virtues'}
  ];

  ngOnInit() {}

}
