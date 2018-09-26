
/**
 * A wrapper for objects (Items, Printers, NetworkConnections, etc.) that should be displayed in a [[GenericTableComponent]].
 */
export class TableElement<T> {

  /**
   * the object to be displayed in a table, that this class is a wrapper for.
   */
  obj: T;

  /**
   * use in modals and whatnot, for selecting rows in a table. True iff selected.
   */
   selected: boolean;

  /**
   * @param obj the object to be packaged as a TableElement.
   *            Can be any type, but should have a meaningful toString method, and must have all the methods and/or attributes
   *            mentioned in the table's Column definitions.
   */
  constructor(obj: T) {
    this.obj = obj;
    this.selected = false;
  }

  /**
   * @return a meaningful string representing the object this TableElement is holding, if one exists.
   *         (one should always exist)
   */
  toString(): string {
    if (this.obj) {
      return this.obj.toString();
    }
    return "[empty TableElement object]";
  }

}
