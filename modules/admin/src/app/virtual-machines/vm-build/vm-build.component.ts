import { Component, OnInit } from '@angular/core';
import { FormControl, FormGroup } from '@angular/forms';

@Component({
  selector: 'app-vm-build',
  templateUrl: './vm-build.component.html',
  styleUrls: ['./vm-build.component.css']
})
export class VmBuildComponent implements OnInit {
  osValue: string;
  osInfo: string;
  osList = [
    { "os_name":"CentOS", "os_info":"http://mirror.centos.org/centos/7/os/x86_64/Packages/" } ,
    { "os_name":"Debian", "os_info": "https://packages.debian.org/stable/" },
    { "os_name":"Fedora", "os_info":"https://apps.fedoraproject.org/" },
    { "os_name":"Red Hat Linux", "os_info":"https://access.redhat.com/downloads" },
    { "os_name":"Windows", "os_info":"https://www.microsoft.com/en-us/windows/" }
  ];
  constructor() { }

  ngOnInit() {
  }

}
