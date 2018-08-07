import { DictList } from './dictionary.model';

export abstract class Item {
  id: string;
  name: string;
  status: string;
  enabled: boolean;
  childIDs: string[];
  children: DictList<Item>;
  childNamesHtml: string; //updated when data is pulled
  modDate: string;


  constructor(id: string, name: string) {
    this.id = id;
    this.setName(name);
    this.status = "enabled";
    this.enabled = true;
    this.childIDs = [];
    this.modDate = ''
    // TODO
    // this.children = new DictList<Item>();

    this.childNamesHtml = "";
  }

  formatChildNames(allChildren: DictList<Item>) {
    this.childNamesHtml = this.getSpecifiedItemsHTML(this.childIDs, allChildren);
  }

  getSpecifiedItemsHTML(desiredIDs: string[], allItems: DictList<Item>) {
    if (desiredIDs.length < 1 || allItems.length < 1) {
      return "";
    }
    let names: string[] = [];
    for (let id of desiredIDs) {
      //TODO ask Chris L and make sure I'm understanding correctly how disabling
      //templates ought to work
      let line = allItems.get(id).name;

      //NOTE if enabled is undefined, !...enabled evaluates to true, while
      //...enabled === false evaluates to false. We need the latter.
      if ( allItems.get(id).enabled === false ) {
        line += "  (disabled)";
      }
      /**
      This would be nice, but would require nontrivial changes. Remember
      templates can be added to multiple things. So we can't set a "parentStatus"
      flag, because more than one parent may have a copy of any given template.
      Anyway, the only time this would appear is in the Applications section of
      the virtues list.
      */
      // if (allItems.get(id).parentStatus === 'disabled') {
      //   line += "  (unavail.)";
      // }
      names.push(line);
    }
    return this.formatListToHTML(names);
  }

  formatListToHTML(l: string[]): string {
    let listString: string = "<ul>";

    for (let line of l) {
      listString += "<li> " + line + " </li>";
    }
    listString += "</ul>";
    return listString;
  }

  setName(s: string) {
    this.name = s;
  }

  //Overriden by User
  getName(): string {
    return this.name;
  }

  //Overriden by User
  getID(): string {
    return this.id;
  }
}
