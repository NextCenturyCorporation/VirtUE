import { DictList } from './dictionary.model';

export abstract class Item {
  id: string;
  name: string;
  //status as a string as well as a bool makes filtering much easier - since 3 possible
  //values need to be matched against it ('enabled', 'disabled', and '*').
  status: string;
  enabled: boolean;
  childIDs: string[];
  children: DictList<Item>;
  //this holds the children's names as an html list, and is updated whenever
  //data is pulled. The places where children are displayed (both list and form
  //pages) need to be able to treat the "children" column of that table the same
  //way they treat the name column - it has to be entirely contained in either
  //an attribute, or a function. If it weren't built beforehand and saved, it'd
  //get re-calculated everytime angular updated the page (which happens on mouse
  //movement).
  childNamesHTML: string;
  modDate: string;

  constructor() {
    this.status = "enabled";
    this.enabled = true;
    this.childIDs = [];
    this.modDate = ''

    this.childNamesHTML = "";

    this.children = new DictList<Item>();
  }

  buildChildren(childDataset: DictList<Item>) {
    this.children = new DictList<Item>();
    for (let childID of this.childIDs) {
      let child: Item = childDataset.get(childID);
      if (child) {
        this.children.add(childID, child);
      }
      else {
        console.log("child ID in item not found in dataset. I.e., if this is for a user, \
it has a virtue ID attached to it which doesn't exist in the backend data.")
      }
    }

    this.childNamesHTML = this.buildChildNamesHtmlList(childDataset);
  }

  //this builds a string of the item's childrens' names, as an html list.
  buildChildNamesHtmlList(allChildren: DictList<Item>): string {
    let names: string[] = this.getChildNames(this.childIDs, allChildren);
    return this.formatListToHtml(names);
  }

  getChildNames(desiredIDs: string[], allItems: DictList<Item>): string[] {
    if (desiredIDs.length < 1 || allItems.asList().length < 1) {
      return [];
    }
    let names: string[] = [];
    for (let id of desiredIDs) {
      let line = allItems.get(id).name;

      //NOTE if enabled is undefined, !{..}.enabled evaluates to true, while
      // {..}.enabled === false evaluates to false. We need the latter.
      // enabled should never be undefined though.
      if ( allItems.get(id).enabled === false ) {
        line += "  (disabled)";
      }

      names.push(line);
    }
    return names;
  }


  formatListToHtml(l: string[]): string {
    let listString: string = "";
    for (let line of l) {
      listString += "<li> " + line + " </li>";
    }
    return listString;
  }

  //Overriden by User
  getName(): string {
    return this.name;
  }

  //Overriden by User
  getID(): string {
    return this.id;
  }

  removeChild(id: string, index: number): void {
    this.children.remove(id);
    this.childIDs.splice(index, 1);
  }
}
