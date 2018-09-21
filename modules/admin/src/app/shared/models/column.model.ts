import { Item } from './item.model';

/**
 *
 */
export class TextContent: {
  /**  */
  formatElement: (elem: TableElement) => string,
  /**  */
  link?: (elem: TableElement) => void,
  subMenuOpts?: () => SubMenuOptions[]
}

/**
 *
 */
export class ListContent {
  list: (elem: TableElement) => TableElement[],
  formatElement?: (elem: TableElement) => string,
  link?: (elem: TableElement) => void
}

/**
 *
 */
export class CheckboxContent {
  toggleableFieldName: string
}

/**
 *
 */
export class DropdownContent {
  /**  */
  fieldName: string,
  /**  */
  dropdownList: (elem: TableElement) => TableElement[],
  /**  */
  formatElement?: (elem: TableElement) => string,
}

/**
 *
 */
export class InputFieldContent {
  /**  */
  inputFieldName: string
}

/**
 *
 */
export class IconContent {
  /**  */
  iconPath: string,
  /**  */
  link?: (elem: TableElement) => void
}

/**
 *
 */
export class RadioButtonContent {
  /**  */
  fieldName: string,
  /**
   * must set all other related boolean radioButton fields to false
   * Should be the same function for each group of radio buttons
   */
  function?: (elem: TableElement) => void
}



/**
 * @class
 * This class represents a column in a [[GenericTableComponent]]
 *
 * A set of these are defined by each component that contains a table.
 *
 *
 *  This way, if we define
 *                    column.content = {iconPath = "/something/icon.ico"}
 *    and a helper method:
 *                    isIcon(x): boolean {
 *                      return (x instanceof Icon);
 *                    }
 *  Then we can just check whether content is a certain type, without having to check what it isn't, and without
 *    having to include a bunch of extra 'undefined's in every column constructor.
 */
export class Column {

  /**
   * The attribute within Item to be displayed in this column, if not a list and formatValue is not defined.
   */
  name: string;

  /**
   * The label to be displayed for this column in the table header
   */
  prettyName: string;

  /**
   * The width this column should be; the sum for all columns must be 12.
   */
  colWidth: number;

  /**
   * The default sort direction when a new column is sorted on.
   * Either 'asc' or 'desc'. Usually 'asc'.
   */
  sortDefault: string;

  /**
   * A function returning a list of Items to be displayed for each Item in the column.
   * (usually a list of the item's children)
   */
  list?: (i: Item) => Item[];

  /**
   * This function must take an item, and return the value that should be displayed for that Item in that column.
   * If the column should display a list of Items in each row, this function (if specified) will be used to generate
   * a label for each Item in that list.
   */
  formatValue?: (item: Item) => string;

  /**
   * If this function is defined, it is called when the label for an item in this column is clicked.
   * It will be passed the item as a parameter.
   *
   * Because of scoping rules, if you're defining a table which is an attribute of a class called
   * Bar, and you want a function in Bar, 'foo(i : Item)', to be called when the label in a column is clicked,
   * then you'll want to define link as '(i: Item) => this.foo(i)'. If you just pass in 'this.foo', the function
   * will be called, but under the scope of the Column object.
   *
   * If this is not defined, the label will simply show up as text.
   *
   * If this column holds a list, this function will be applied to each value in that list.
   */
  link?: (item: Item) => void;

  /**
   * @param name The attribute within Item to be displayed in this column, if not a list and formatValue is not defined.
   * @param prettyName The label to be displayed for this column in the table header
   * @param colWidth The width this column should be; the sum for all columns must be 12.
   * @param sortDefault The default direction to sort this column on. Ignored if this column holds lists. Optional.
   * @param list A function which returns a list of Items to be displayed in each entry for this column. Optional.
   * @param formatValue A function that takes an item, and returns a string to be displayed in this column for that item. Optional.
   * @param link A function that is called when this item is clicked. Optional.
   *
   */
  constructor(name: string, prettyName: string, colWidth: number, sortDefault?: string,
    formatValue?: (item: Item) => string, list?: (i: Item) => Item[], link?: (item: Item) => void ) {
      this.name = name;
      this.prettyName = prettyName;
      this.colWidth = colWidth;

      if (sortDefault) {this.sortDefault = sortDefault;}

      if (formatValue) {this.formatValue = formatValue;}

      if (list) {this.list = list;}

      if (link) {this.link = link;}
    }
}
