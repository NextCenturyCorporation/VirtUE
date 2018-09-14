import { DictList } from './dictionary.model';
import { Mode, Datasets } from '../enums/enums';

/**
 * #uncommented
 * @class
 */
export abstract class Item {

  /** #uncommented */
  id: string;

  /** #uncommented */
  name: string;

  /** #uncommented */
  // status as a string, in addition to the bool, makes filtering much easier - 3 possible
  // values need to be matched against it ('enabled', 'disabled', and '*').
  status: string;

  /** #uncommented */
  enabled: boolean;

  /** #uncommented */
  childIDs: string[];

  /** #uncommented */
  children: DictList<Item>;

  /** #uncommented */
  modDate: string;

  /** #uncommented */
  // a link to the parent domain for this item - '/users', '/virtues', etc.
  parentDomain: string;

  /**
   * #uncommented
   * @param
   *
   * @return
   */
  constructor() {
    this.status = "enabled";
    this.enabled = true;
    this.childIDs = [];
    this.modDate = '';

    this.parentDomain = "NA";

    this.children = new DictList<Item>();
  }

  /**
   * #uncommented
   * @param
   *
   * @return
   */
// Note that childDataset refers to the child of the items being built
  buildChildren(childDataset: DictList<Item>): void {
    this.children = new DictList<Item>();
    if (this.childIDs) {
      for (let childID of this.childIDs) {
        let child: Item = childDataset.get(childID);
        if (child) {
          this.children.add(childID, child);
        } else {
          console.log("child ID in item not found in dataset. I.e., if this is for a user, \
          it has a virtue ID attached to it which doesn't exist in the backend data.");
        }
      }
    }
  }

  /**
   * Overriden by [[User]]
   *
   * @return the item's displayable/human-readable name. Not guarenteed to be unique.
   */
  getName(): string {
    return this.name;
  }

  /**
  * @return a link to where this Item can be viewed/edited/duplicated - Something like "users/{view/edit/duplicate}/Phillip"
   *
   * @param mode the [[Mode]] this Item should be opened in.
   *
   */
  getPageRoute(mode: Mode): string {
    if (mode === Mode.CREATE) {
      console.log("Invalid request for item page route - can't open an existing page in 'Create' mode.");
      return this.parentDomain;
    }
    return this.parentDomain + '/' + mode.toLowerCase() + '/' + this.getID();
  }

  /**
   * Overriden by User
   * @return a unique identifier for this Item.
   */
  getID(): string {
    return this.id;
  }

  /**
   * #uncommented
   * @param
   *
   * @return
   */
  removeChild(id: string, index?: number): void {
    this.children.remove(id);
    if (index) {
      this.childIDs.splice(index, 1);
    } else {
      this.childIDs.splice(this.childIDs.indexOf(id), 1);
    }

  }

  /**
   * #uncommented
   * @param
   *
   * @return
   */
  toString(): string {
    return this.getName();
  }
}
