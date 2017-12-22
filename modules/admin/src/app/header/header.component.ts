import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.css']
})
export class HeaderComponent implements OnInit {

  navigation = [
    {value: 'Dashboard', link: '/dashboard'},
    {value: 'Configuration', link: '/config'},
    {value: 'Users', link: '/users'},
    {value: 'Virtues', link: '/virtues'}
  ];
  
  ngOnInit() {}

}
