import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-vm-edit',
  templateUrl: './vm-edit.component.html',
  styleUrls: ['./vm-edit.component.css']
})
export class VmEditComponent implements OnInit {

  vm: { id: number };
  vm_name: string = 'test';
  osValue: string;
  osInfo: string;
  osList = [
    { "id":10, "os_name":"CentOS", "os_info":"http://mirror.centos.org/centos/7/os/x86_64/Packages/" } ,
    { "id":11, "os_name":"Debian", "os_info": "https://packages.debian.org/stable/" },
    { "id":12, "os_name":"Fedora", "os_info":"https://apps.fedoraproject.org/" },
    { "id":13, "os_name":"Red Hat Linux", "os_info":"https://access.redhat.com/downloads" },
    { "id":14, "os_name":"Windows", "os_info":"https://www.microsoft.com/en-us/windows/" }
  ];
  constructor(private router: ActivatedRoute) { }

  ngOnInit() {
    this.vm = {
      id: this.router.snapshot.params['id']
    };
  }

}
