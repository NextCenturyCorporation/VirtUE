import { Component, OnInit } from '@angular/core';
import { JsondataService } from '../../data/jsondata.service'

@Component({
  selector: 'app-vm-list',
  templateUrl: './vm-list.component.html',
  styleUrls: ['./vm-list.component.css']
})
export class VmListComponent implements OnInit {

  noListData = false;
  vmlist: string;
  vms = [
     {
       "id": 1,
       "vm_name": "Chrome",
       "vm_os": "Debian",
       "vm_packages": "{'package 1', 'package 2', 'package 3', 'package n...'}",
       "vm_timestamp": "2017-12-05T19:57:01.052901",
       "vm_status": "enabled"
     },
     {
       "id": 2,
       "vm_name": "GIMP",
       "vm_os": "Debian",
       "vm_packages": "{'package 1', 'package 2', 'package 3', 'package n...'}",
       "vm_timestamp": "2017-12-05T19:57:01.052901",
       "vm_status": "enabled"
     },
     {
       "id": 3,
       "vm_name": "LastPass",
       "vm_os": "Debian",
       "vm_packages": "{'package 1', 'package 2', 'package 3', 'package n...'}",
       "vm_timestamp": "2017-12-05T19:57:01.052901",
       "vm_status": "enabled"
     },
     {
       "id": 4,
       "vm_name": "Microsoft Word",
       "vm_os": "Windows",
       "vm_packages": "{'package 1', 'package 2', 'package 3', 'package n...'}",
       "vm_timestamp": "2017-12-05T19:57:01.052901",
       "vm_status": "enabled"
     },
     {
       "id": 5,
       "vm_name": "Microsoft Excel",
       "vm_os": "Windows",
       "vm_packages": "{'package 1', 'package 2', 'package 3', 'package n...'}",
       "vm_timestamp": "2017-12-05T19:57:01.052901",
       "vm_status": "enabled"
     },
     {
       "id": 6,
       "vm_name": "Microsoft Outlook",
       "vm_os": "Windows",
       "vm_packages": "{'package 1', 'package 2', 'package 3', 'package n...'}",
       "vm_timestamp": "2017-12-05T19:57:01.052901",
       "vm_status": "enabled"
     },
     {
       "id": 7,
       "vm_name": "Microsoft Poject",
       "vm_os": "Windows",
       "vm_packages": "{'package 1', 'package 2', 'package 3', 'package n...'}",
       "vm_timestamp": "2017-12-05T19:57:01.052901",
       "vm_status": "enabled"
     },
     {
       "id": 8,
       "vm_name": "Microsoft PowerPoint",
       "vm_os": "Windows",
       "vm_packages": "{'package 1', 'package 2', 'package 3', 'package n...'}",
       "vm_timestamp": "2017-12-05T19:57:01.052901",
       "vm_status": "enabled"
     },
     {
       "id": 9,
       "vm_name": "Microsoft Teams",
       "vm_os": "Windows",
       "vm_packages": "{'package 1', 'package 2', 'package 3', 'package n...'}",
       "vm_timestamp": "2017-12-05T19:57:01.052901",
       "vm_status": "enabled"
     }
  ];

  vmLength = this.vms.length;

  constructor(  ) { }



  ngOnInit() {

  }

}
