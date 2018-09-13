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
  // this holds the children's names as an html list, and is updated whenever
  // data is pulled. The places where children are displayed (both list and form
  // pages) need to be able to treat the "children" column of that table the same
  // way they treat the name column - it has to be entirely contained in either
  // an attribute, or a function. If it weren't built beforehand and saved, it'd
  // get re-calculated everytime angular updated the page (which happens on mouse
  // movement).
  childNamesHTML: string;

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

    this.childNamesHTML = "";

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
    for (let childID of this.childIDs) {
      let child: Item = childDataset.get(childID);
      if (child) {
        this.children.add(childID, child);
      } else {
        console.log("child ID in item not found in dataset. I.e., if this is for a user, \
it has a virtue ID attached to it which doesn't exist in the backend data.");
      }
    }

    this.childNamesHTML = this.buildChildNamesHtmlList(childDataset);
  }

  /**
   * #uncommented
   * @param
   *
   * @return
   */
  // this builds a string of the item's childrens' names, as an html list.
  buildChildNamesHtmlList(allChildren: DictList<Item>): string {
    let names: string[] = this.getChildNames(this.childIDs, allChildren);
    return this.formatListToHtml(names);
  }

  /**
   * #uncommented
   * @param
   *
   * @return
   */
  getChildNames(desiredIDs: string[], allItems: DictList<Item>): string[] {
    if (desiredIDs.length < 1 || allItems.asList().length < 1) {
      return [];
    }
    let names: string[] = [];
    for (let id of desiredIDs) {
      let line = allItems.get(id).getName();

      // NOTE if enabled is undefined, !{..}.enabled evaluates to true, while
      // {..}.enabled === false evaluates to false. We need the latter.
      // enabled should never be undefined though.
      if ( allItems.get(id).enabled === false ) {
        line += "  (disabled)";
      }

      names.push(line);
    }
    return names;
  }


  /**
   * #uncommented
   * @param
   *
   * @return
   */
  formatListToHtml(list: string[]): string {
    let listString: string = "";
    for (let line of list) {
      listString += "<li> " + line + " </li>";
    }
    return listString;
  }

  /**
   * #uncommented
   * @param
   *
   * @return
   */
  // Overriden by User
  getName(): string {
    return this.name;
  }

  /**
   * #uncommented
   * @param
   *
   * @return
   */
  // eturns a link to where the item can be viewed/edited/duplicated.
  // Something like "users/{view/edit/etc}/Phillip"
  getPageRoute(mode: Mode): string {
    if (mode === Mode.CREATE) {
      console.log("Invalid request for item page route - can't open an existing page in 'Create' mode.");
      return this.parentDomain;
    }
    return this.parentDomain + '/' + mode.toLowerCase() + '/' + this.getID();
  }

  /**
   * #uncommented
   * @param
   *
   * @return
   */
  // Overriden by User
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
