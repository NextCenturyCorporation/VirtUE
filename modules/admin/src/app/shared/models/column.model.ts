import { Item } from './item.model';

export class Column {
  name: string;
  prettyName: string;
  isList: boolean;
  sortDefault: string;
  colWidth: number;
  formatValue: (item: Item) => string;
}
