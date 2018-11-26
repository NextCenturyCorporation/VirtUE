import { DictList } from './dictionary.model';

import { IndexedObj } from './indexedObj.model';
import { Toggleable } from './toggleable.interface';
import { DatasetNames } from '../abstracts/gen-data-page/datasetNames.enum';

import { Mode } from '../abstracts/gen-form/mode.enum';

/**
 * @class
 * This class represents a generic item - a [[User]], [[Virtue]], [[VirtualMachine]], or [[Application]].
 *
 * These are constructed from records retrieved from the backend in [[GenericDataPageComponent.recursivePullData]].
 *  Those records have the same data as Items, but the names of attributes aren't consistent, and so a conversion
*  is done in each Item-subclass' constructor, and a conversion back in the inherited [[IndexedObj.getInBackendFormat]] method
 *
 * The generic ways of interacting with Items are:
 *  - Methods:
 *    - [[getID]]()
 *    - [[getName]]()
 *    - [[setID]]()
 *    - #uncommented
 *  - Attributes:
 *    - [[enabled]]
 */
export abstract class Item extends IndexedObj implements Toggleable {

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
  enabled: boolean = true;

  /** A full Date, of the last time this record was changed on the backend. */
  modificationDate: Date;

  /**
   * a shortened form the the last modificaiton date, suitible for display
   * Note: don't sort anything on this field, sort on a format that progresses like
   *    year:month:day:hour:minute:second
   */
  readableModificationDate: string = "";

  /** a link to the parent domain for this item - i.e. '/users', '/virtues', etc. */
  parentDomain: string = "NA";


  constructor() {
    super();
  }

  /**
   * @return the item's displayable/human-readable name. Not guaranteed to be unique.
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
   * @return the url for the view page for this item
   */
  getViewURL(): string {
    return this.getPageRoute(Mode.VIEW);
  }

  /**
   * @return the url for the edit page for this item
   */
  getEditURL(): string {
    return this.getPageRoute(Mode.EDIT);
  }

  /**
   * @return the url at which one could create a duplicate of this item
   */
  getDupURL(): string {
    return this.getPageRoute(Mode.DUPLICATE);
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
   * See [[ItemFormComponent.ngOnInit]]()
   */
  setID(id: string) {
    this.id = id;
  }

  /**
   * This removes the child with the given id and index from the relevant list.
   * If we ever add multiple lists of the same type (like two lists that hold virtues) then something here will need to change.
   * Maybe just pass in an extra parameter saying the type of dataset
   * @param id the id of the child to be removed
   * @param datasetType the name of the dataset relating to the list the item should be removed from.
   *        i.e., if you want to remove printer with ID=12345 from Virtue V1's printer list, call
   *             `V1.removeChild( "1234", DatasetNames.PRINTERS );`
   */
  removeChild(id: string, datasetType: DatasetNames): void {

    // remove that id.
    let childIDList: string[] = this.getRelatedIDList(datasetType);
    let index = childIDList.indexOf(id);
    if (index > -1) {
       childIDList.splice(index, 1);
    }

    // and remove the actual item.
    this.getRelatedDict(datasetType).remove(id);
  }

  abstract removeUnspecifiedChild(childObj: IndexedObj);

  /**
   * Eventually this will be what is used by table to display the item's name - once the table is made to display classes that
   *  implement a simple interface, consisting of at least toString().
   * @return a string reperesenting this Item, usable for display to a user or when debugging.
   */
  toString(): string {
    return this.getName();
  }

}
