

import { DictList } from './dictionary.model';

/**
 * @class
 * Represents an object that can be pulled from the backend, and which upon load needs to be linked to other objects.
 * This is an generalization of [[Item]].
 *
 * Note this linking must not be circular. This essentially lets us make a chain of dependencies.
 * It needs to be able to be self-referential though. So we'll have to deal with dependencies in two stages.
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
   * @param childSet a Datasets enum that lets this object decide which attribute to give childObjects to.
   * @param childObjects a DictList of the objects referenced by one of this IndexedObj's list of IDs.
   */
  abstract buildAttribute( childSet: Datasets, childObjects: DictList<IndexedObj> ): void;

  /**
   * @param childSet a Datasets enum telling the objec which list of IDs to return
   * @return a list of IDs that this object needs from the input childSet.
   */
  abstract getSetIDs( childSet: Datasets ): string[];


}
