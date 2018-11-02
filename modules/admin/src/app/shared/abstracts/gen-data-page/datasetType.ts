import { DatasetNames } from './datasetNames.enum';

/**
 * @interface
 * This defines a dataset type
 */
export interface DatasetType {

  /** The url on the backend from which to request this sort of dataset */
  serviceUrl: string;

  /** the class members of this dataset should be created as. Apparently needs to be 'any'. */
  class: any;

  /**
   * The name of the dataset attribute within this class. E.g. 'allApps', 'allUsers', etc.
   *
   * Specified as a Dataset enum to encapsulate that.
   */
  datasetName: DatasetNames;

  /**
   * The dataset which members of this dataset have links to, and so which must be already loaded in order to fully-flesh
   * out the items in this dataset.
   * At the moment it's just used to build item.children from item.childIDs for each item in *this* dataset.
   */
  depends: DatasetNames[];
}
