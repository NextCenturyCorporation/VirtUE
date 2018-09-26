/**
 * An enum for [[GenericTableComponent]]s - should the table allow selection, and if so, how many can be selected at a time?
 */
export enum SelectionMode {
  OFF = "off",          // disallow selection
  SINGLE = "single",  // allow selection via radio buttons
  MULTI = "multi"     // allow selection through checkboxes
}
