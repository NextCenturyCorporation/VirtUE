import { Item } from './item.model';

export class Column {
  name: string;
  prettyName: string;
  list: (i: Item) => Item[];
  sortDefault: string;
  colWidth: number;
  formatValue?: (item: Item) => string;
  link?: (item: Item) => void; // called when clicked, if not undefined. Currently only the first name value is ever a link.

  constructor(name: string, prettyName: string, list: (i: Item) => Item[], sortDefault: string,
    colWidth: number, formatValue?: (item: Item) => string, link?: (item: Item) => void ) {
      this.name = name;
      this.prettyName = prettyName;
      this.list = list;
      this.sortDefault = sortDefault;
      this.colWidth = colWidth;
      if (formatValue) {
        this.formatValue = formatValue;
      }
      if (link) {
        this.link = link;
      }
    }
}
