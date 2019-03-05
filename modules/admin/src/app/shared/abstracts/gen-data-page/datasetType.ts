import { DatasetNames } from './datasetNames.enum';
import { Subdomains } from '../../services/subdomains.enum';

import { DictList, Dict } from '../../models/dictionary.model';

import { Item } from '../../models/item.model';
import { IndexedObj } from '../../models/indexedObj.model';
import { User } from '../../models/user.model';
import { Virtue } from '../../models/virtue.model';
import { VirtueInstance } from '../../models/virtue-instance.model';
import { VirtualMachine } from '../../models/vm.model';
import { VirtualMachineInstance } from '../../models/vm-instance.model';
import { Application } from '../../models/application.model';
import { Printer } from '../../models/printer.model';
import { FileSystem } from '../../models/fileSystem.model';
import { SensorRecord } from '../../models/sensor.model';

/**
 * @interface
 * This defines a dataset type.
 *
 * Note that the readonly modifiers are not supposed to make anything secure. They're just to help prevent accidental programmer-error.
 *    (They don't exist in the javascript, and even in TS the compiler can only detect a violation in simple cases; finding
 *     at compilation time if a readonly property is being set reduces to the halting problem.)
 */
export interface DatasetType {

  /** the class members of this dataset should be created as. Apparently needs to be 'any'. */
  readonly class: any;

  readonly datasetName: DatasetNames;

  /** The backend endpoint subdomain for this dataset */
  readonly subdomain: Subdomains;
  /**
   * The datasets which members of this dataset have links to, and so which must be already loaded in order to fully-flesh
   * out the items in this dataset.
   */
  readonly depends: DatasetNames[];
}


/**
 * This defines the metadata about all loadable datasets, so they can be used generically when pulling data.
 */
export class DatasetsMeta {

  /**
   * Not sure if this matters, but something to consider #TODO - could someone change these, clientside, to request data in a way that
   * breaks the system? Or that gives them access to things they shouldn't? Probably not the latter, because they'll generally
   * have access to everything if they can get to the workbench at all. You could see or destroy anything through the workbench directly.
   * Perhaps you could send something that causes an error on the virtueadmin server though? I don't think that would do anything.
   */
  private dict: Dict<DatasetType>;
  constructor() {
    this.dict = new Dict<DatasetType>();
    this.dict[DatasetNames.PRINTERS] = {
      class: Printer,
      datasetName: DatasetNames.PRINTERS,
      subdomain: Subdomains.PRINTERS,
      depends: []
    };
    this.dict[DatasetNames.FILE_SYSTEMS] = {
      class: FileSystem,
      datasetName: DatasetNames.FILE_SYSTEMS,
      subdomain: Subdomains.FILE_SYSTEMS,
      depends: []
    };
    this.dict[DatasetNames.APPS] = {
      class: Application,
      datasetName: DatasetNames.APPS,
      subdomain: Subdomains.APPS,
      depends: []
    };
    this.dict[DatasetNames.VM_TS] = {
      class: VirtualMachine,
      datasetName: DatasetNames.VM_TS,
      subdomain: Subdomains.VM_TS,
      depends: [DatasetNames.APPS]
    };
    this.dict[DatasetNames.VIRTUE_TS] = {
      class: Virtue,
      datasetName: DatasetNames.VIRTUE_TS,
      subdomain: Subdomains.VIRTUE_TS,
      depends: [DatasetNames.VM_TS, DatasetNames.PRINTERS, DatasetNames.FILE_SYSTEMS]
    };
    this.dict[DatasetNames.USERS] = {
      class: User,
      datasetName: DatasetNames.USERS,
      subdomain: Subdomains.USERS,
      depends: [DatasetNames.VIRTUE_TS, DatasetNames.VIRTUES]
    };
    this.dict[DatasetNames.VMS] = {
      class: VirtualMachineInstance,
      datasetName: DatasetNames.VMS,
      subdomain: Subdomains.VMS,
      depends: [DatasetNames.VM_TS]
    };
    this.dict[DatasetNames.VIRTUES] = {
      class: VirtueInstance,
      datasetName: DatasetNames.VIRTUES,
      subdomain: Subdomains.VIRTUES,
      depends: [DatasetNames.VIRTUE_TS, DatasetNames.VMS]
    };
    this.dict[DatasetNames.SENSORS] = {
      class: SensorRecord,
      datasetName: DatasetNames.SENSORS,
      subdomain: Subdomains.SENSORS,
      depends: [DatasetNames.VIRTUES]
    };
  }

  /**
   * @return the static list of datasets
   */
  getDatasets(): Dict<DatasetType> {
    return this.dict;
  }
}
