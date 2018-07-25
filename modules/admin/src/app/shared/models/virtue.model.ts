
import { VirtualMachine } from '../../shared/models/vm.model';

export class Virtue {
  id: string;
  name: string;
  enabled: boolean;
  vmIDs: any[];
  vms: VirtualMachine[];
  version: string;
  lastEditor: string;
  lastModification: any;
  awsTemplateName: string;
  appIDs: any[];
  color: string;


  constructor() {
    this.vmIDs = [];
    this.vms = new Array<VirtualMachine>();
    this.enabled = true;
    this.appIDs = [];
  }
}
