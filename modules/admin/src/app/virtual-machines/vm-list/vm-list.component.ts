import { Component, OnInit } from '@angular/core';
import { JsondataService } from '../data/jsondata.service'

@Component({
  selector: 'app-vm-list',
  templateUrl: './vm-list.component.html',
  styleUrls: ['./vm-list.component.css']
})
export class VmListComponent implements OnInit {

  noListData = false;
  vms = [
     {
       "vm_name": "Chrome",
       "vm_os": "Debian",
       "vm_packages": "{'package 1', 'package 2', 'package 3', 'package n...'}",
       "vm_timestamp": "2017-12-05T19:57:01.052901",
       "vm_status": "enabled"
     },
     {
       "vm_name": "GIMP",
       "vm_os": "Debian",
       "vm_packages": "{'package 1', 'package 2', 'package 3', 'package n...'}",
       "vm_timestamp": "2017-12-05T19:57:01.052901",
       "vm_status": "enabled"
     },
     {
       "vm_name": "LastPass",
       "vm_os": "Debian",
       "vm_packages": "{'package 1', 'package 2', 'package 3', 'package n...'}",
       "vm_timestamp": "2017-12-05T19:57:01.052901",
       "vm_status": "enabled"
     },
     {
       "vm_name": "Microsoft Word",
       "vm_os": "Windows",
       "vm_packages": "{'package 1', 'package 2', 'package 3', 'package n...'}",
       "vm_timestamp": "2017-12-05T19:57:01.052901",
       "vm_status": "enabled"
     },
     {
       "vm_name": "Microsoft Excel",
       "vm_os": "Windows",
       "vm_packages": "{'package 1', 'package 2', 'package 3', 'package n...'}",
       "vm_timestamp": "2017-12-05T19:57:01.052901",
       "vm_status": "enabled"
     },
     {
       "vm_name": "Microsoft PowerPoint",
       "vm_os": "Windows",
       "vm_packages": "{'package 1', 'package 2', 'package 3', 'package n...'}",
       "vm_timestamp": "2017-12-05T19:57:01.052901",
       "vm_status": "enabled"
     },
     {
       "vm_name": "Microsoft Teams",
       "vm_os": "Windows",
       "vm_packages": "{'package 1', 'package 2', 'package 3', 'package n...'}",
       "vm_timestamp": "2017-12-05T19:57:01.052901",
       "vm_status": "enabled"
     }
  ];


  constructor(  ) { }



  ngOnInit() {

  }

}
