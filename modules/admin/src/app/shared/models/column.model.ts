import { Item } from './item.model';
import { SubMenuOptions } from './subMenuOptions.model';

/**
 * This allows for a consistent way to reference the direction a table is sorting elements - either 'asc' or 'desc'.
 */
export enum SORT_DIR {
  ASC = "asc",
  DESC = "desc"
}

/**
 * @class
 * This class represents a column in a [[GenericTableComponent]]
 *
 * Each component that contains a table must define and pass in a list of Columns.
 *
 * This is now abstract, so each calling class will define what types of columns they want for their table, by specifying the
 * subclass, rather than by passing Column some opaque set of defined and undefined parameters, in turn necessitating many
 * if-else checks in the Table's display code, to make sure the right types of columns are displayed the right way.
 *
 * As mentioned in [[GenericTableComponent]], it'd be nice if each column could be responsible for its own display code,
 * but it seems there's no good way in angular to do that. So while functionality is defined by the classes that create Column
 * instances, the display code must be handled entirely by the table.
 *
 * NOTE: All Column subclasses must have a unique set of required attributes, so that their type can be distinguished at
 *       runtime.
 */
export abstract class Column {

  constructor(
      /** The label to be displayed for this column in the table header */
      public label: string,
      /** The width this column should take up in the table; the sum for all columns must be 12. */
      public width: number
    ) {}
}


/**
 * This abstract adds a 'link' attribute to the Column class, which is called when the HTML element within that column, in a particular
 * row, is clicked. See [[GenericTableComponent]]
 */
abstract class LinkableColumn extends Column {
  constructor(
    /** see [[Column.label]] for details */
    label: string,
    /** see [[Column.width]] for details */
    width: number,
    /**
     * If this function is defined, it is called when the label for an TableElement in this column is clicked.
     * It will be passed the object whose label was clicked, as a parameter.
     * This object could be of any type, but will usually be an Item.
     *
     * Because of scoping rules, if you're defining a table which is an attribute of a class called
     * Bar, and you want a function in Bar, 'foo(e : TableElement)', to be called when the label in a column is clicked,
     * then you'll want to define link as '(e: TableElement) => this.foo(e)'. If you just pass in 'this.foo', the function
     * will be called, with the correct parameter, but under the scope of the Column object. So if you don't need the scope
     * of the page that holds the table, you can just pass in 'this.foo'.
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
 * This interface allows the html to check whether a given Column subclass is sortable, without having to check
 * what particular subclass it is. The only thing that matters is whether that column has been given a [[sortField]].
 *
 * NOTE: having a sortField field will make this system treat the class as sortable.
 */
interface Sortable {
  /** A function that returns a string representing the given object, which this column should use when sorting the objects. */
  sortField: (elem: any) => string;
}

/**
 * This class represents a column, holding a single piece of text for each row.
 *
 * The text can be made to perform some arbitrary caller-defined function upon click.
 *
 * This type of Column can also be used to sort the table, and can be given a submenu to show on each row - a dynamic
 * list of clickable links that show up under the text string in each row of this column.
 */
export class TextColumn extends LinkableColumn implements Sortable {
  /** A function that returns a string representing the given object, which this column should use when sorting the objects. */
  public sortField: (elem: any) => string;

  constructor(
    /** see [[Column.label]] for details */
    label: string,
    /** see [[Column.width]] for details */
    width: number,
    /** This function takes the object held by a TableElement, and return the value that should be displayed in
     * that column, for that TableElement. */
    public formatElement: (elem: any) => string,
    /** The default sort direction when a new column is sorted on. Usually ASC. */
    public sortDefault: SORT_DIR,
    /** see parent [[LinkableColumn]] */
    link?: (elem: any) => void,
    /** A function to return a list of SubMenuOptions which should appear, beneath the label in every row, in this column */
    public subMenuOpts?: () => SubMenuOptions[],
    /** see [[sortField]]. If not specified, sortField uses the formatElement function - sorting by the displayed text. */
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
 * This Column displays a list for row/TableElement. The list's elements' string representations can be formatted arbitrarily,
 * or made clickable. Is not sortable - simply displays the list in the order returned by [[getList]]
 *
 * Is generic, but the type is not necessarily the same as the Table - E is the type of objects to be shown in the list,
 * not necessarily the type of the objects held by the table's TableElements.
 */
export class ListColumn<E> extends LinkableColumn {
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
    public getList: (elem: any) => E[],
    /**
     * Is used to get a display-able string for each object in list
     * @return a string that represents the given object. Usually a name.
     */
    public formatElements: (elem: E) => string,
    /** see parent [[LinkableColumn]] */
    link?: (elem: E) => void
  ) {
    super(label, width, link);
  }
}

/**
 * This Column holds a dropdown box. Each row in this column can select a value from the same set of options.
 * Is generic, but the type is not necessarily the same as the Table - E is the type of objects to be shown in the dropdown,
 * not necessarily the type of the objects held by the table's TableElements.
 * The dropdown box is marked as required, always.
 */
export class DropdownColumn<E> extends Column implements Sortable {
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
    /** a function to return a list of possible options. */
    public dropdownList: (elem: any) => E[],
    /**
     * A function that takes a object, and returns a string to be displayed in this column for that element.
     * Parameter is the object held by the TableElement.
     */
    public formatElement: (elem: E) => string,

    /**
     * A function that returns a string representing the given object, which this column should use when sorting the objects.
     */
    public sortField: (elem: any) => string
  ) {
    super(label, width);
  }
}

/**
 * This column holds a list of checkboxes, which are mapped to a given attribute within each TableElement shown in this Column.
 * The checkboxes can also be arbitrarily disabled, based on some system or TableElement state.
 */
export class CheckboxColumn extends Column {
  constructor(
    /** @param label see [[Column.label]] for details */
    label: string,
    /** @param width see [[Column.width]] for details */
    width: number,
    /** the attribute within the object held by each TableElement which should be mapped to the checkbox in that TableElement's row */
    public toggleableFieldName: string,
    /**
     * A function that deteremines whether or not a particular TableElement's checkbox should be disabled, based on that TableElement's
     * current state.
     */
    public disabled?: (obj: any) => boolean,
    /**  Do something on check/uncheck. */
    public onChange?: (obj: any, checked: boolean) => any
  ) {
    super(label, width);
  }
}

/**
 * This Column holds an input field mapped to an attribute within the object held by each TableElement.
 * The input field is marked as required, always.
 * Could be improved by allowing the definition of a function to check the field for validity. Not sure how to use that though.
 */
export class InputFieldColumn extends Column implements Sortable {
  constructor(
    /** @param label see [[Column.label]] for details */
    label: string,
    /** @param width see [[Column.width]] for details */
    width: number,
    /** the name of the attribute within the object held by each TableElement that this input field should be mapped to. */
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
 * This Column just holds a clickable icon.
 *
 */
export class IconColumn extends LinkableColumn {
  constructor(
    /** @param label see [[Column.label]] for details */
    label: string,
    /** @param width see [[Column.width]] for details */
    width: number,
    /** The name of the svgIcon to display. */
    public iconName: string,
    /** see parent [[LinkableColumn]] Note that this is not optional. */
    link: (elem: any) => void
  ) {
    super(label, width, link);
  }
}

/**
 * This Column allows for the addition of radio buttons to a row - where perhaps each of three columns has a radio button in it, and the
 * user must select one of those three options.
 *
 * See code and comments within gen-table.component.html for use.
 *
 * Essentially, giving multiple RadioButtonColumns the same object (generally that object is an attribute within a particular
 * TableElement.obj) to watch, puts them in the same group. Each column has a value to be given to the watched object, if that column's
 * radio button is selected.
 */
export class RadioButtonColumn extends Column {
  constructor(
    /** @param label see [[Column.label]] for details */
    label: string,
    /** @param width see [[Column.width]] for details */
    width: number,
    /** the field to be watched/set by this column's radio button. */
    public fieldName: string,
    /** the value to be given to [[fieldName]], if this column is selected. */
    public value: string
    ) {
      super(label, width);
    }
}

/**
 * A column that just serves to label other columns. Like an empty column labelled "Security Level:", placed before a set of
 * RadioButtonColumns.
 */
export class LabelColumn extends Column {
  constructor(
    /** see [[Column.label]] for details */
    label: string,
    /** see [[Column.width]] for details */
    width: number,
  ) {
    super(label, width);
  }
}

/**
 * A blank column, that just exists to put space between other columns.
 * The label is optional here.
 */
export class BlankColumn extends Column {
  constructor(
    /** see [[Column.width]] for details */
    width: number,
  ) {
    super("", width);
  }
}
