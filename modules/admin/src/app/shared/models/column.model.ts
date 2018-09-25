import { TableElement } from './tableElement.model';
import { Item } from './item.model';
import { SubMenuOptions } from './subMenuOptions.model';

/**
 * Note that when used in html, the actual values must be used, rather than the enums, unless we want to introduce
 * more bloat. So don't change these values without grepping the html files for 'asc' and 'desc'.
 */
export enum SORT_DIR {
  ASC = "asc",
  DESC = "desc"
}

/**
 * @class
 * This class represents a column in a [[GenericTableComponent]]
 *
 * A set of these are defined by each component that contains a table.
 * This is now abstract, so each calling class will define what types of columns they want for their table by specifying the
 * subclass, rather than by passing Column some opaque set of defined and undefined parameters.
 *
 * NOTE: All Column subclasses must have a unique set of required attributes, so that their type can be distinguished at
 *       runtime.
 */
export abstract class Column {

  constructor(
      /** @param label The label to be displayed for this column in the table header */
      public label: string,
      /** @param width The width this column should be; the sum for all columns must be 12. */
      public width: number
    ) {}
}


/**
 *
 */
abstract class LinkableColumn extends Column {
  constructor(
    /** @param label see [[Column.label]] for details */
    label: string,
    /** @param width see [[Column.width]] for details */
    width: number,
    /**
     * If this function is defined, it is called when the label for an TableElement in this column is clicked.
     * It will be passed the object whose label was clicked, as a parameter.
     * This object could be of any type, but will usually be an Item.
     *
     * Because of scoping rules, if you're defining a table which is an attribute of a class called
     * Bar, and you want a function in Bar, 'foo(i : TableElement)', to be called when the label in a column is clicked,
     * then you'll want to define link as '(i: Item) => this.foo(i)'. If you just pass in 'this.foo', the function
     * will be called, but under the scope of the Column object.
     *
     * If this is not defined, the label will simply show up as text.
     *
     * If this column holds a list, this function will be applied to each value in that list.
     */
      public link?: (elem: any) => void
    ) {
      super(label, width);
    }
}

/**
 * #uncommented
 *
 * NOTE: having a sortField field will make this system treat the class as sortable.
 */
interface Sortable {
  /** A function that returns a string representing the given object, which this column should use when sorting the objects. */
  sortField: (elem: any) => string
}

/**
 *
 *
 */
export class TextColumn extends LinkableColumn implements Sortable {
  /** A function that returns a string representing the given object, which this column should use when sorting the objects. */
  public sortField: (elem: any) => string;

  constructor(
    /** @param label see [[Column.label]] for details */
    label: string,
    /** @param width see [[Column.width]] for details */
    width: number,
    /** This function takes the object held by a TableElement, and return the value that should be displayed in
     * that column, for that TableElement. */
    public formatElement: (elem: any) => string,
    /** The default sort direction when a new column is sorted on. Usually ASC. */
    public sortDefault: SORT_DIR,
    /** see parent [[LinkableColumn]] */
    link?: (elem: any) => void,
    /** #uncommented */
    public subMenuOpts?: () => SubMenuOptions[],
    /** see [[sortField]] */
    sortField?: (elem: any) => string
  ) {
    super(label, width, link);
    if (sortField) {
      this.sortField = sortField;
    }
    else {
      this.sortField = this.formatElement;
    }
  }
}

/**
 *
 */
export class ListColumn<T> extends LinkableColumn {
  constructor(
    /** @param label see [[Column.label]] for details */
    label: string,
    /** @param width see [[Column.width]] for details */
    width: number,
    /**
     * A function returning a list of objects to be displayed for each TableElement in the column.
     * (usually a list of the tableElement's children)
     * Parameter is the object held by the TableElement.
     */
    public list: (elem: any) => T[],
    /**
     * Is used to get a display-able string for each object in list
     * @return a string that represents the given object. Usually a name.
     */
    public formatElements: (elem: T) => string,
    /** see parent [[LinkableColumn]] */
    link?: (elem: T) => void
  ) {
    super(label, width, link);
  }
}

/**
 *
 */
export class DropdownColumn<T> extends Column implements Sortable {
  constructor(
    /** @param label see [[Column.label]] for details */
    label: string,
    /** @param width see [[Column.width]] for details */
    width: number,
    /**
     * @param fieldName the name of the attribute of the object in each TableElement, that should be set by this dropdown box
     * e.g. if the table holds items within its TableElements, and the dropdown box should set the item's status, then
     * fieldName should hold the string 'enabled'.
     */
    public fieldName: string,
    /** #uncommented */
    public dropdownList: (elem: any) => T[],
    /**
     * A function that takes a object, and returns a string to be displayed in this column for that element.
     * Parameter is the object held by the TableElement.
     */
    public formatElement: (elem: T) => string,

    /**
     * A function that returns a string representing the given object, which this column should use when sorting the objects.
     */
    public sortField: (elem: any) => string
  ){
    super(label, width);
  }
}

/**
*
*/
export class CheckboxColumn extends Column {
  /** #uncommented */
  // public onClick?: ((obj: any) => void) = ((obj) => {});

  constructor(
    /** @param label see [[Column.label]] for details */
    label: string,
    /** @param width see [[Column.width]] for details */
    width: number,
    /** #uncommented */
    public toggleableFieldName: string,
    /** #uncommented */
    public disabled?: (obj: any) => boolean
  ) {
    super(label, width);
  }
}

/**
 *
 */
export class InputFieldColumn extends Column implements Sortable {
  constructor(
    /** @param label see [[Column.label]] for details */
    label: string,
    /** @param width see [[Column.width]] for details */
    width: number,
    /**  */
    public inputFieldName: string,

    /**
     * A function that returns a string representing the given object, which this column should use when sorting the objects.
     */
    public sortField: (elem: any) => string
    ) {
      super(label, width);
    }
}

/**
 *
 */
export class IconColumn extends LinkableColumn {
  constructor(
    /** @param label see [[Column.label]] for details */
    label: string,
    /** @param width see [[Column.width]] for details */
    width: number,
    /** #uncommented */
    public iconName: string,
    /** see parent [[LinkableColumn]] */
    link?: (elem: any) => void
  ) {
    super(label, width, link);
  }
}

/**
 *
 *
 *
 */
export class RadioButtonColumn extends Column {
  constructor(
    /** @param label see [[Column.label]] for details */
    label: string,
    /** @param width see [[Column.width]] for details */
    width: number,
    /** #uncommented */
    public fieldName: string,
    /** #uncommented */
    public value: string
    ) {
      super(label, width);
    }
}
