

import { DictList } from './dictionary.model';
import { DatasetNames } from '../abstracts/gen-data-page/datasetNames.enum';

/**
 * @class
 * Represents an object that can be pulled from the backend, and which has references (via lists of IDs) to other IndexObjs.
 * This is an generalization of [[Item]].
 *
 * Note this linking currently isn't set up to handle circular dependencies. We need to be able to create a chain of dependencies.
 * It needs to be able to be self-referential though, per the spec. So we'll have to deal with dependencies in two stages.
 *
 * E.g. A certain page needs to display a list of users and a list of virtues, along with all data about those virtues.
 *      The Virtues have virtual machines, printers, and file systems.
 *      The virtual machines have applications.
 *
 *      Thus, we'd need to first load the applications, use them to set up the virtual machines, and then load the printers and
 *        file systems.
 *      It can then set up the virtues, and thereafter set up the Users.
 *
 *
 * This type of of object is needed to load all data in the same way.
 */
export abstract class IndexedObj {

  /**
   * @return a unique ID for this object.
   */
  abstract getID(): string;

  /**
   * @return The DatasetNames enum that this object belongs to
   */
  abstract getDatasetName(): string;

  /**
   * Empty by default. Items with children can override it.
   *
   * @param datasetName a DatasetNames enum that lets this object
   * @param dataset #uncommented
   */
  buildAttribute( datasetName: DatasetNames, dataset: DictList<IndexedObj> ): void {}


  /**
   * The following two functions are generally just used in tables to display the indexedObjs attached
   * to an [[IndexedObj]] (ie the `children`), and the indexedObjs attached to each of those (i.e. the `grandchildren`).
   */
  protected getChildren(childDatasetName: DatasetNames): IndexedObj[] {
    return this.getRelatedDict(childDatasetName).asList();
  }

  /**
   * @return A list (not a set) of the requested type of this obj's children's children.
   * Example: For an input User, look through that user's Virtue children, and generate a list of all of those
   * virtues' collective Printers.
   */
  protected getGrandChildren(childDatasetName: DatasetNames, grandChildDatasetName: DatasetNames): IndexedObj[] {
    let grandchildren: IndexedObj[] = [];
    for (let c of this.getChildren(childDatasetName)) {
      grandchildren = grandchildren.concat(c.getChildren(grandChildDatasetName));
    }
    return grandchildren;
  }

  /**
   * Empty by default. Items with children can override it and make it return what it should.
   * @param datasetName a DatasetNames enum telling the object which DictList of objects to return
   * @return a DictList holding a set of child objects held by this.
   */
  getRelatedDict( datasetName: DatasetNames ): DictList<IndexedObj> {
    return new DictList<IndexedObj>();
  }

  /**
   * Empty by default. Items with children can override it.
   * @param datasetName a DatasetNames enum telling the object which list of IDs to return
   */
  getRelatedIDList( datasetName: DatasetNames ): string[] {
    return [];
  }

  getFormatForSave() {
    return JSON.stringify(this.getInBackendFormat());
  }

  /**
   * Overridden by some subclasses
   */
  protected getInBackendFormat(): any {
    return this;
  }
}
