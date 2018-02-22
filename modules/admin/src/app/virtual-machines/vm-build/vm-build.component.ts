import { Component, OnInit } from '@angular/core';
import { FormControl, FormGroup } from '@angular/forms';
import { VirtualMachineService } from '../../shared/services/vm.service';

@Component({
  selector: 'app-vm-build',
  templateUrl: './vm-build.component.html',
  styleUrls: ['./vm-build.component.css'],
  providers: [ VirtualMachineService ]
})
export class VmBuildComponent implements OnInit {
  osValue: string;
  osInfo: string;
  osList = [
    { 'id': 10, 'os_name': 'CentOS', 'os_info': 'http://mirror.centos.org/centos/7/os/x86_64/Packages/' } ,
    { 'id': 11, 'os_name': 'Debian', 'os_info': 'https://packages.debian.org/stable/' },
    { 'id': 12, 'os_name': 'Fedora', 'os_info': 'https://apps.fedoraproject.org/' },
    { 'id': 13, 'os_name': 'Red Hat Linux', 'os_info': 'https://access.redhat.com/downloads' },
    { 'id': 14, 'os_name': 'Windows', 'os_info': 'https://www.microsoft.com/en-us/windows/' }
  ];

  constructor(
    private vmService: VirtualMachineService
  ) { }

  ngOnInit() {
  }

  onBuildVM(name, os, packages) {
    // const buildDate: Date = new Date();
    // const pkgs = packages.replace(/\n/g,'|');
    // const vmFields='{'vm_name':''+name+''},{'vm_os':''+os+''},{'vm_packages':''+pkgs+''},
    // {'vm_timestamp':''+buildDate+''},{'vm_status':'disabled'}';
    // console.log('new values: '+name+','+os+','+pkgs+','+buildDate);
    // this.jsondataService.addNewData('vms',vmFields);
  }

}
