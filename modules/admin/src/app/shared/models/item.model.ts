import { DictList } from './dictionary.model';
import { Mode, Datasets } from '../enums/enums';

/**
 * @class
 * This class represents a generic item - a [[User]], [[Virtue]], [[VirtualMachine]], or [[Application]].
 *
 * It is one of the main building blocks of this system at the moment - everything is configured to
 * hold and use Items.
 *
 * These are constructed from records retrieved from the backend in [[GenericDataPageComponent.recursivePullData]].
 *  Those records have the same data as Items, but the names of attributes aren't consistent, and so a conversion
 *  is done in each Item-subclass' constructor.
 *
 * The generic ways of interacting with Items are:
 *  - Methods:
 *    - [[getID]]()
 *    - [[getName]]()
 *    - [[setID]]()
 *  - Attributes:
 *    - [[enabled]]
 *    - [[children]]
 *    - [[childIDs]]
 */
export abstract class Item {

  /** a unique identifing string - not used by user */
  id: string;

  /** a readable name for the item - is treated as ID for [[User]] */
  name: string;

  /**
   * The item's status, as a boolean.
   *  - Users that are disabled should be unable to log in or take any action
   *  - Virtue templates - being disabled prevents any derived instance from starting/showing up in a user's list,
   *    and can't be added to any more User objects.
   *  - VM templates - being disabled prevents their being added to any more Virtue templates, or their applications
   *    from showing up on a user's list.
   *  - Applications can't be disabled - should be done at the VM/Virtue level.
   */
  enabled: boolean;

  /** a list of the IDs of this Item's children, saved to and loaded from backend */
  childIDs: string[];

  /**
   * A [[DictList]] of references to this Item's children, as listed in childIDs.
   * Not saved to backend.
   */
  children: DictList<Item>;

  /**
   * a shortened form the the last modificaiton date, suitible for display
   * Note: don't sort anything on this field, sort on a format that progresses like
   *    year:month:day:hour:minute:second
   */
  modDate: string;

  /** a link to the parent domain for this item - '/users', '/virtues', etc. */
  parentDomain: string;

  /**
   * Gives default values for necessary attributes.
   */
  constructor() {
    this.enabled = true;
    this.modDate = '';

    this.parentDomain = "NA";

    this.childIDs = [];
    this.children = new DictList<Item>();
  }

  /**
   * This function uses [[childIDs]] and the input dataset to build a list of references to the Items
   * identified in childIDs.
   * @param childDataset the dataset in which to to look for child IDs.
   */
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
   * @return an identifying and unique, but not necessarily readable, string for this Item.
   */
  getID(): string {
    return this.id;
  }

  /**
   * Sets the item's ID
   * @param id the ID to set this to.
   * Overridden by [[User.setID]]()
   *
   * Currently used only when a form loads, to make sure that the same attribute gets initialized with an ID as
   * would be querried by getID().
   * See [[GenericFormComponent.ngOnInit]]()
   */
  setID(id: string) {
    this.id = id;
  }

  /**
   * This removes the child with the given id and index from childIDs and children.
   * @param id the id of the Item to be remove
   * @param index optional, calculated if not given. Just saves on time if it's available and can be passed in directly.
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
   * Eventually this will be what is used by table to display the item's name - once the table is made to display classes that
   *  implement a simple interface, consisting of at least toString().
   * @return a string reperesenting this Item, usable for display to a user or when debugging.
   */
  toString(): string {
    return this.getName();
  }
}
