import { Item } from './item.model';

/**
 * @class
 * This class represents a column in a [[GenericTableComponent]]
 *
 * A set of these are defined by each component that contains a table.
 *
 *
 * Ok. So this needs to be changed to have:
 *    - prettyName
 *    - colWidth
 *    - link (optional)
 *    - content, an object holding objects which must fit into some category (define this as a type that can be any of the below combos)
 *      - possible elements:
 *        - text: string                  [the name of an attribute to be used directly]
 *        - list: ((elem: TableElement) => TableElement[])
 *        - formatElement: ((elem: TableElement) => string)
 *        - toggleableField: string       [the name, as a string, of a boolean attribute in that type of table element]
 *        - dropdownField: string         [the name, as a string, of an attribute in that type of table element]
 *        - dropdownList: TableElement[]  [the name, as a string, of a boolean attribute in that type of table element]
 *        - inputField: string            [the name, as a string, of a string-type attribute in that type of table element]
 *        - icon: string                  [a path to the icon]
 *      - possible combinations:
 *        - //text
 *        - //text and link
 *        - formatElement
 *        - formatElement and link
 *        - list
 *        - list and link
 *        - list, formatValue, and link
 *        - toggleableField
 *        - dropdownField, dropdownList, and formatElement
 *        - inputField
 *        - icon and link
 *      - Remove option for directly requesting an attribute. Use formatElement instead - just pass in ((item: Item) => item.whatever;)
 *  So there'll only have to be one check for each possible type of column component, in the table.
 *    - classes:
 *                    Text: {
 *                      type: string = "text",
 *                      formatElement: (elem: TableElement) => string,
 *                      link?: (elem: TableElement) => void,
 *                      subMenuOpts?: () => SubMenuOptions[]
*                     } |
 *                    List: {
 *                      list: (elem: TableElement) => TableElement[],
 *                      formatElement?: (elem: TableElement) => string,
 *                      link?: (elem: TableElement) => void
 *                    } |
 *                    Checkbox: {
 *                      toggleableFieldName: string
*                     } |
 *                    Dropdown: {
 *                      fieldName: string,
 *                      dropdownList: (elem: TableElement) => TableElement[],
 *                      formatElement?: (elem: TableElement) => string,
*                     } |
 *                    InputField: {
 *                      inputFieldName: string
*                     } |
 *                    Icon: {
 *                      iconPath: string,
 *                      link?: (elem: TableElement) => void
 *                    } |
 *                    RadioButton: {
 *                      fieldName: string,
 *                      // function must set all other related bool fields to false
 *                      function?: (elem: TableElement) => void
 *                    }
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
