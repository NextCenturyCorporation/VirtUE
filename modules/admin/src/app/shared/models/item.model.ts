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
    this.children = new DictList<Item>();

    this.childrenListHTMLstring = "";
  }

  formatChildrenList() {
    if (this.children.getL().length < 1) {
      this.childrenListHTMLstring = "";
      return;
    }

    // console.log("Trying here", item);
    this.childrenListHTMLstring = "<ul>";
    for (let c of this.children.getL()) {
      this.childrenListHTMLstring += "<li> " + c.name + " </li>";
    }
    this.childrenListHTMLstring += "</ul>";
  }

  setName(s: string) {
    this.name = s;
  }

  //Overriden by children
  getName(): string {
    return this.name;
  }
}
