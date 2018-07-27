
import { Item } from './item.model';
import { VirtualMachine } from './vm.model';

export class Virtue extends Item{
  // id: string;
  name: string;
  // enabled: boolean;
  vmIDs: any[];
  vms: VirtualMachine[];
  version: string;
  lastEditor: string;
  lastModification: any;
  awsTemplateName: string;
  appIDs: any[];
  color: string;


  constructor(id: string, name: string) {
    super(id, name);
    this.vmIDs = [];
    this.vms = new Array<VirtualMachine>();
    // this.enabled = true;
    this.appIDs = [];
  }

  getName() {
    return this.name;
  }
}
