export class Column {
  name: string;
  prettyName: string;
  isList: boolean;
  sortDefault: string;
  colWidth: number;
  formatValue: (item: any) => string; //later make this 'any' an Item object. TODO
}
