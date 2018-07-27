
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

  constructor(virtueObj) {
    if (virtueObj) {
      super(virtueObj.id, virtueObj.name);
      this.enabled = virtueObj.enabled;
      this.version = virtueObj.version;
      this.lastEditor = virtueObj.lastEditor;
      this.lastModification = virtueObj.lastModification;
      this.color = virtueObj.color;
      this.status = virtueObj.enabled ? 'enabled' : 'disabled';
    }
    else {
      super('', '');
    }
  }

  getName() {
    return this.name;
  }
}
