import { Component, OnInit } from '@angular/core';
import { FlexLayoutModule } from '@angular/flex-layout';

import { HttpEvent, HttpHandler, HttpRequest } from '@angular/common/http';
import { MatDialog } from '@angular/material';
import { Router } from '@angular/router';
import { Observable } from 'rxjs/Observable';
import 'rxjs/add/observable/of';

import { DialogsComponent } from '../../../dialogs/dialogs.component';

import {
        Column,
        TextColumn,
        ListColumn,
        CheckboxColumn,
        DropdownColumn,
        InputFieldColumn,
        IconColumn,
        RadioButtonColumn,
        SORT_DIR
      } from '../../models/column.model';

import { SelectionMode } from './selectionMode.enum';

import { TableElement } from '../../models/tableElement.model';

import { SubMenuOptions } from '../../models/subMenuOptions.model';
import { DictList } from '../../models/dictionary.model';

import { GenericPageComponent } from '../gen-page/gen-page.component';


/********************************************
 * @class
 * This class represents a generic table, displaying a list of [[TableElements]] all holding the same (generic) type of object.
 *
 * TableElements hold objects, and can be marked as 'selected', if the table's [[selectionMode]] is MULTI or SINGLE.
 * Selections should be set by passing in a list of the objects to be selected (not IDs or anything, to keep this generic)
 * after [[populate]] is called, and the corresponding TableElements will be marked as selected by comparing their held
 * objects with the input objects, using the caller-defined [[equals]] method. For many tables, this equals method just compares ID.
 *
 * Because there doesn't seem to be a way to inject chunks of HTML into another HTML file without extreme obscurity or extensive
 * parent-child interactions, this GenericTable takes responsibility for knowing how to display the different types of Column.
 * Columns are generic enough for each instance to define its own functionality, but if a new column were to be added, relevant display
 * code would need to be added to GenericTable.component.html, in the same place and style as the other Column descriptions.
 * It'd be nice if each column knew how to display itself, but that doesn't appear to be possible.
 *
 * In general:
 *    Columns can be TextColumn, ListColumn, CheckboxColumn, DropdownColumn, InputFieldColumn, IconColumn, RadioButtonColumn
 *
 *
 * Using this table needs three things:
 *   1. Have a table object. Include it in the html file via:
 *           <app\-table #table></app\-table>
 *         and in the parent .ts
 *           @ViewChild(GenericTable) table: GenericTable;
 *     --remember doing it this way means the table gets instantiated when the
 *       containing component's ngOnInit runs, not during the component's constructor.
 *
 *   2. call table.setUp(), once the necessary data is available
 *       (generally is by the time of ngOnInit)
 *
 *   3. set 'elements' to an Item[], once the desired item list is available. It's expected
 *       that the data won't be there instantaneously, but angular should update the table
 *       whenever the data arrives.
 *
 *
 *
 * ********************************************/
@Component({
  selector: 'app\-table',
  templateUrl: './gen-table.component.html',
  styleUrls: ['../gen-page/gen-page.component.css']
})
export class GenericTableComponent<T> {

  /**
   * This defines what columns show up in the table. If supplied, [[GenericPageComponent.formatValue]](i: Item) will be called
   * to get the text for that item for that column. If not supplied, the text will be assumed to be "item.{colData.name}"
   */
  colData: Column[];

  /**
   * This list is what gets actually displayed in the table.
   *
   * Table will automatically update once this is set, and will display an no-data-message in the meantime.
   */
  elements: TableElement<T>[];

  /** used to put a colored bar for everywhere virtues show up */
  hasColoredLabels: boolean;

  /**
   * The filter options that appear above the table; each has text which the option's label, and
   * a value which the status of the [[TableElement]] must match for that filter.
   * Should be made generic and allow filtering on any column.
   * Filters should be made into their own class as well, when that change happens.
   *
   * See note on [[GenericListComponent.getTableFilters]]().
   */
  filterOptions: {text: string, value: string}[];

  /**
   * This is the name of the attribute of the objects held by this table's TableElements that the filter should be applied to.
   * So if this table shows Users, and you want to filter out all Users that don't have a certain username, then this should
   * be 'name' to apply the filter using each User's 'name' value.
   * Currently only the status/enabled column can be filtered on.
   */
  filterColumnName: string = 'enabled';

  /** The current value the table should be filtered by - only matching elements are displayed. */
  filterValue: string = '*';

  filterCondition = (attribute: any) => {return String(attribute) === this.filterValue || this.filterValue === '*'};

  /**
   * Call to re-render the table on a change to filterValue.
   * this just gets toggled, and is passed into the listFilterSort pipe, where it is ignored.
   * The fact that its value changes though, makes angular re-render the table, filtering it based on the currect criteria.
   */
  update: boolean = false;

  /** The message that should show up intead of any table data, when [[elements]] is undefined or empty. */
  noDataMessage: string;

  /** How much of the parent space should the table take up, expressed in 1/12's (e.g., must be between 1 and 12, inclusive) */
  tableWidth: number;

  /** The column which should the table should be sorted by */
  sortColumn: Column;

  /** Whether the table should be sorted in an ascending or descending pattern. Valid values are ASC or DESC */
  sortDirection: string = SORT_DIR.ASC;

  /**
   * Holds a [[SelectionMode]] describing whether rows in the table can be selected, and if so, how many values can be selected.
   * Currently, only valid values are {OFF, SINGLE, MULTI}.
   * Requiring a maximum number of selected rows, or some minimum, should require merging the code that deals with
   * SINGLE and MULTI, and maybe SelectionMode as well, to just be {OFF or ON}, and making the calling object define how
   * many rows can be selected in [[setUp]]
   */
  selectionMode: SelectionMode;

  /**
  * compares two objects of the type held by this table's TableElements, and returns true if they are equal.
  * Caller-defined.
  */
  equals?: (obj1: T, obj2: T) => boolean;

  /**
   * A caller-overrideable function for defining whether the interactable columns/things in this table should be disabled, given
   * some caller-defined table state. Default is a function that returns false - disabling most interactable html objects.
   * Note that this doesn't disable sorting or filtering, or SubMenuOpts. Sub menus are expected to manage their existence
   * through their own "shouldAppear" function.
   */
  editingEnabled: (() => boolean) = () => false;

  /**
   *
   */
   selectedObj: T;

  /**
   * Set all parameters to default parameters for the meantime before the calling class calls [[setUp]]().
   */
  constructor() {
    // create meaningless empty column to prevent exceptions before setUp() is called by the parent component's ngOnInit
    this.sortColumn = new TextColumn("", 0, (e) => e.toString(), SORT_DIR.ASC);
    this.colData = [this.sortColumn];
    this.filterOptions = [];
    this.elements = [];
    // this.subMenuOptions = [];
    this.tableWidth = 12; // default to take up full space in container
    this.selectionMode = SelectionMode.OFF;
  }

  /**
   * Must be called by the object holding this table, passing in the parameters the table
   * needs. 'elements' isn't passed in, because it usually isn't available at render time when the table is built.
   *
   * @param params is bundled into a single object to make it easier for the callee to see what element they're setting to what,
   * and because most elements are necessary.
   *
   */
  setUp(params: {
    /** see this.[[colData]] */
    cols: Column[];

    /** see this.[[subMenuOptions]] */
    // opts: SubMenuOptions[];

    /** see this.[[filterOptions]] */
    filters: {value: string, text: string}[];

    /**
     * tableWidth must be between 1 and 12, inclusive.
     * see this.[[tableWidth]]
     */
    tableWidth: number,

    /** see this.[[noDataMessage]] */
    noDataMsg: string,

    /** see this.[[hasColoredLabels]] */
    coloredLabels?: boolean;

    /** see this.[[editingEnabled]] */
    editingEnabled?: () => boolean

    /** Many tables aren't used for selection, but if selection of rows should be allow, all of the following should be defined. */
    selectionOptions?: {
      /** What selection mode this table should be set up in */
      selectionMode: SelectionMode,
      /** see this.[[equals]]
       * Like pretty much all passed functions in typescript, this should be passed as () => someFunc(), in order to call someFunc
       * within the calling class, using the calling class' scope, as opposed to the scope of the Column or Table.
       */
      equals: (obj1: T, obj2: T) => boolean
      }
    }
  ): void {
    this.colData = params.cols;

    if (params.coloredLabels !== undefined) {
      this.hasColoredLabels = params.coloredLabels;
    }
    else {
      this.hasColoredLabels = false;
    }

    this.filterOptions = params.filters;
    this.noDataMessage = params.noDataMsg;

    if ((params.tableWidth >= 1 && params.tableWidth <= 12)) {
      this.tableWidth = params.tableWidth;
    }

    if (params.editingEnabled !== undefined) {
      this.editingEnabled = params.editingEnabled;
    }

    if (params.selectionOptions) {
      this.selectionMode = params.selectionOptions.selectionMode;
      this.equals = params.selectionOptions.equals;
    }
    if (this.colData && this.colData.length > 0 && (this.colData[0] instanceof TextColumn)) {
      this.sortColumn = this.colData[0];
    }
  }

  /**
   * remove everything from [[elements]]
   */
  clear(): void {
    this.elements = [];
  }

  /**
   * Populates the table with the input objects. Table is cleared first.
   * @param dataset the objects to be put into the table.
   */
  populate(dataset: any[]): void {
    this.clear();
    for (let d of dataset) {
      this.elements.push(new TableElement(d));
    }
  }

  /**
   * This should be called at least right after populate, if anything should be marked as selected.
   * @param selected must be a list of objects of the type the table is holding.
   */
  setSelections(selected: T[]): void {
    if (!selected) {
      console.log("Invalid input selections.");
      return;
    }

    if (this.selectionMode === SelectionMode.MULTI) {
      for (let elem of this.elements) {
        elem.selected = false;
        for (let selectedObj of selected) {
          if (this.equals(elem.obj, selectedObj)) {
            elem.selected = true;
            break;
          }
        }
      }
    }
    else if (this.selectionMode === SelectionMode.SINGLE) {
      if (selected.length !== 1) {
        console.log("SINGLE selection mode expects an input 'selected' list with exactly one value.");
        return;
      }
      this.selectedObj = selected[0];
    }
    else { // selectionMode is OFF
      console.log("selectionMode is not MULTI or SINGLE - Can't set selections.");
      return;
    }

  }

  /**
   * Called whenever the user checks or unchecks the "master" checkbox in the table's header.
   * Either marks all elements as selected, or unmarks them, as appropriate.
   *
   * @param checked true if the user just checked the box, false if the user unchecked it.
   */
  selectAll(checked): void {
    if (checked) {
      for (let elem of this.elements) {
        elem.selected = true;
      }
    } else {
      for (let elem of this.elements) {
        elem.selected = false;
      }
    }
  }

  /**
   * @return a list holding the currently-selected object(s).
   */
  getSelections(): T[] {
    let selections: T[] = [];

    if (this.selectionMode === SelectionMode.MULTI) {
      for (let elem of this.elements) {
        if (elem.selected) {
          selections.push(elem.obj);
        }
      }
    }
    else if (this.selectionMode === SelectionMode.SINGLE) {
      if (this.selectedObj !== undefined) {
        selections.push(this.selectedObj);
      }
    }

    return selections;
  }

  /**
   * Called whenever the user checks or unchecks the "master" checkbox in a CheckboxColumn's header.
   * @param checkCol the column whose header was checked/unchecked
   * @param checked true if the user just checked the box, false if the user unchecked it.
   */
  checkAllInColumn(checkCol: CheckboxColumn, event): void {
    for (let elem of this.elements) {
      // If the box was checked, and this current checkbox isn't disabled
      if (event.checked && !(checkCol.disabled && checkCol.disabled(elem.obj))) {
        elem.obj[checkCol.toggleableFieldName] = true;
      }
      else {
        elem.obj[checkCol.toggleableFieldName] = false;
      }
    }

  }

  /**
   * @return true iff all elements in this column are checked.
   */
  allCheckedInColumn(checkCol: CheckboxColumn): boolean {
    let numberEnabled = 0;
    for (let elem of this.elements) {
      // if the box itself is enabled
      if ( ! (checkCol.disabled && checkCol.disabled(elem.obj) ) ) {
        // add to counter
        numberEnabled++;

        // if the enabled box is unchecked, return false
        if (elem.obj[checkCol.toggleableFieldName] === false) {
          return false;
        }
      }
    }
    // if we're here, then all boxes are either diabled, or checked.
    // We don't want to show the master box as being checked if all boxes are just disabled
    // though, so:

    return numberEnabled !== 0;
  }

  /**
   * sets the watched attribute [[filterValue]], causing angular to refresh the page
   * and run the filter/sorter again - which is called via the pipe '|' character
   * in gen-table.component.html
   *
   * @param filterValue the new value to filter the list by. Only matching elements are kept, unless value is '*'.
   */
  filterList(filterValue: string): void {
    this.filterValue = filterValue;
    // The value of [[update]] is irrelevant - merely the fact that its value changes will refresh the table.
    this.update = !this.update;
  }

  /**
   * Called when the user clicks a sortable column's header label.
   * Columns are sortable iff their entries do not contain a list, and they have a default sort specified.
   * Sets sort direction to column default if the column is different from the current column being sorted on,
   * and reverses the sort direction if it's the same.
   *
   * @param sortColumn the column to sort the table by
   */
  setColumnSortDirection(sortColumn: Column): void {
    // If the user clicked the currently-active column
    if (this.sortColumn === sortColumn) {
      this.reverseSorting();
    } else {
      this.sortColumn = sortColumn;
    }
  }

  /**
   * Toggles [[sortDirection]] between ASC and DESC.
   */
  reverseSorting(): void {
    if (this.sortDirection === SORT_DIR.ASC) {
      this.sortDirection = SORT_DIR.DESC;
    } else {
      this.sortDirection = SORT_DIR.ASC;
    }
  }

  /**
   * See https://github.com/angular/angular/issues/10423 and/or https://stackoverflow.com/questions/40314732
   * and, less helpfully, the actual docs: https://angular.io/guide/template-syntax#ngfor-with-trackby
   * Essentially, Angular's ngFor sometimes tracks the elements it iterates over by their value (?), as opposed
   * to their index, and so if you put a ngModel on (apparently) any part of an element within an ngFor, it loses track (??) of
   * that item and hangs - as in, Angular hangs. And the containing tab needs to be killed either through the browser's
   * tab manager, or the browser needs to be killed at the system level (xkill, system process manager, kill, killall, etc.).
   * This is prevented by manually telling it track things by index, using the below code, adding "; trackBy: indexTracker" to the
   * end of the ngFor statement.
   * ...
   *
   * @param index an index, automagically passed in.
   * @param value a value, auto{black}magically passed in.
   * @return the index that was passed in.
   */
  indexTracker(index: number, value: any): number {
    return index;
  }

  /**
   * Each column in the table can define a list of options to show up on each row, within that column. See [[Column.subMenuOpts]].
   * The column has a list of options that can show up, but whether any option should show up can depend on the state of the
   * object in that row.
   * e.g., you may want a different menu to show up for disabled row-elements, than for enabled ones. We'd like to display
   * this dynamic list nicely, with one '|' between each option, and it's much easier to filter the list before trying to display it,
   * than to try to define (in the html) when a '|' should show up, given that any (or all) element(s) of the unfiltered list may be
   * hidden.
   */
  filterMenuOptions(menuOpts: SubMenuOptions[], obj: any): SubMenuOptions[] {
    let filteredMenu: SubMenuOptions[] = [];

    for (let o of menuOpts) {
      if (o.shouldAppear(obj)) {
        filteredMenu.push(o);
      }
    }
    return filteredMenu;
  }

  /**
   * @param obj the [[Column]] content whose type we want to check.
   * @return true iff obj has the necessary attributes of a TextColumn object.
   */
  isText(obj: any) {
    return obj instanceof TextColumn;
  }

  /**
   * @param obj the [[Column]] content whose type we want to check.
   * @return true iff obj has the necessary attributes of a ListColumn object.
   */
  isList(obj: any) {
    return obj instanceof ListColumn;
  }

  /**
   * @param obj the [[Column]] content whose type we want to check.
   * @return true iff obj has the necessary attributes of a CheckboxColumn object.
   */
  isCheckbox(obj: any) {
    return obj instanceof CheckboxColumn;
  }

  /**
   * @param obj the [[Column]] content whose type we want to check.
   * @return true iff obj has the necessary attributes of a DropdownColumn object.
   */
  isDropdown(obj: any) {
    return obj instanceof DropdownColumn;
  }

  /**
   * @param obj the [[Column]] content whose type we want to check.
   * @return true iff obj has the necessary attributes of a InputFieldColumn object.
   */
  isInputField(obj: any) {
    return obj instanceof InputFieldColumn;
  }

  /**
   * @param obj the [[Column]] content whose type we want to check.
   * @return true iff obj has the necessary attributes of a IconColumn object.
   */
  isIcon(obj: any) {
    return obj instanceof IconColumn;
  }

  /**
   * @param obj the [[Column]] content whose type we want to check.
   * @return true iff obj has the necessary attributes of a RadioButtonColumn object.
   */
  isRadioButton(obj: any) {
    return obj instanceof RadioButtonColumn;
  }

  /**
   * @param obj the [[Column]] content whose type we want to check.
   * @return true iff obj is a sortable columns - only sortable columns have a "formatElement" field.
   */
  isSortable(obj: any) {
    return 'sortField' in obj;
  }

  /**
   * Check what direction the table is currently being sorted.
   * @return true iff [[sortDirection]] is SORT_DIR.ASC
   */
  sortingAscending(): boolean {
    return this.sortDirection === SORT_DIR.ASC;
  }
}
