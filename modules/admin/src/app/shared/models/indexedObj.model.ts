

import { DictList } from './dictionary.model';
import { DatasetNames } from '../abstracts/gen-data-page/datasetNames.enum';

/**
 * @class
 * Represents an object that can be pulled from the backend, and which upon load needs to be linked to other objects.
 * This is an generalization of [[Item]].
 *
 * Note this linking must not be circular. We need to be able to create a chain of dependencies.
 * It needs to be able to be self-referential though, per the spec. So we'll have to deal with dependencies in two stages.
 *
 *
 * E.g. A certain page needs to display a list of users and a list of virtues. The Virtues have virtual machines, printers, and file systems.
 * The virtual machines have applications.
 *
 * Thus, we'd need to first load the applications, use them to set up the virtual machines, and then load the printers and file systems.
 * It can then set up the virtues, and thereafter set up the Users.
 *
 *
 * This type of of object is needed to load all data in the same way.
 */
export abstract class IndexedObj {

  /**
   * @return a unique ID for this type of object.
   */
  abstract getID(): string;

  /**
   * Empty by default. Items with children can override it.
   *
   * @param datasetName a DatasetNames enum that lets this object
   * @param dataset #uncommented
   */
  buildAttribute( datasetName: DatasetNames, dataset: DictList<IndexedObj> ): void {}


  /**
   * Empty by default. Items with children can override it and make it return what it should.
   * @param datasetName a DatasetNames enum telling the object which DictList of objects to return
   * @return a DictList holding a set of child objects held by this.
   */
  getRelatedDict( datasetName: DatasetNames ): DictList<IndexedObj> {
    return new DictList<IndexedObj>();
  }

  /**
   * Empty by default. Items with children can override it and make it return what it should.
   * @param datasetName a DatasetNames enum telling the object which list of IDs to return
   * @return a list of IDs that this object needs from the input childSet.
   */
  getRelatedIDList( datasetName: DatasetNames ): string[] {
    return [];
  }
}
