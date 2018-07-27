import { DictList } from './dictionary.model';

export abstract class Item {
  id: string;
  name: string;
  status: string;
  enabled: boolean;
  childIDs: string[];
  children: DictList<Item>;
  childrenListHTMLstring: string; //updated when data is pulled


  constructor(id: string, name: string) {
    this.id = id;
    this.setName(name);
    this.status = "enabled";
    this.enabled = true;
    this.childIDs = [];
    //
    // this.children = new DictList<Item>();

    this.childrenListHTMLstring = "";
  }

  formatChildrenList(allChildren: DictList<Item>) {
    // console.log(this.childIDs);
    if (this.childIDs.length < 1 || allChildren.length < 1) {
      // console.log("quitting");
      this.childrenListHTMLstring = "";
      return;
    }

    // console.log("Trying here", item);
    this.childrenListHTMLstring = "<ul>";
    for (let cID of this.childIDs) {
      // console.log(cID, allChildren.get(cID));
      this.childrenListHTMLstring += "<li> " + allChildren.get(cID).name + " </li>";
    }
    this.childrenListHTMLstring += "</ul>";
    // console.log(this.childrenListHTMLstring);
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
