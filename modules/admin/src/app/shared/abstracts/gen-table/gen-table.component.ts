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

import { SELECTION_MODE } from './selectionMode.enum';

import { TableElement } from '../../models/tableElement.model';

import { SubMenuOptions } from '../../models/subMenuOptions.model';
import { DictList } from '../../models/dictionary.model';

import { GenericPageComponent } from '../gen-page/gen-page.component';


/********************************************
 * @class
 * This class represents a table, displaying a list of Items.
 *
 * #uncommented switch to TableElement, describe how Columns now work, how the table needs to know how to display columns
 *    (as opposed to columns knowing how to display themselves)
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
 *       whenever it arrives.
 *
 * Ideally, this should be made yet more generic by having it hold something like TableElements,
 * an interface that specifies an html object to be represented by (label, link, checkbox, icon), and some binding method.
 * Note that for selectable tables, table elements will need some sort of ID.
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
   * This is a list of the clickable links/options that show up under the element in the
   * first column of each row in the table
   */
  // subMenuOptions: SubMenuOptions[];

  /**
   * This list is what gets actually displayed in the table.
   *
   * Table will automatically update once this is set, and will display an no-data-message in the meantime.
   */
  elements: TableElement[];

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
   * Should the table allow selection, and if so, how many can be selected at a time?
   */
  selectionMode: SELECTION_MODE;

  /**
  * compares two objects of the type held by this table's TableElements, and returns true if they are equal. Caller-defined.
  */
  equals?: (obj1: T, obj2: T) => boolean;

  /**
   * #uncommented
   */
  editingEnabled: (() => boolean) = () => false;

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
    this.selectionMode = SELECTION_MODE.OFF;
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

    /**  */
    selectionOptions?: {
      /** What mode  */
      selectionMode: SELECTION_MODE,
      /** see this.[[getObjectID]] */
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
   * #uncommented
   */
  clear(): void {
    this.elements = [];
  }

  /**
   * #uncommented
   */
  populate(dataset: any[]): void {
    this.elements = [];
    for (let d of dataset) {
      this.elements.push(new TableElement(d));
    }
  }

  /**
   * This should be called at least right after populate, if anything should be marked as selected.
   */
  setSelections(selected): void {
    if (this.selectionMode === SELECTION_MODE.OFF) {
      console.log("selectionMode is OFF - Can't set selections.");
      return;
    }

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

  getSelections(): T[] {
    let selections: T[] = [];
    for (let elem of this.elements) {
      if (elem.selected) {
        selections.push(elem.obj);
      }
    }
    return selections;
  }

  radioSelectionChange(event: any, elem: TableElement): void {
    console.log(event, elem);
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
   * @param obj the [[Column]] content whose type we want to check.
   * @return true iff obj has the necessary attributes of a TextColumn object.
   */
  isText(obj: any) {
    return obj instanceof TextColumn;
  }

  /**
   * @param obj the [[Column]] content whose type we want to check.
   * @return true iff obj has the necessary attributes of a TextColumn object.
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
    return 'formatElement' in obj;
  }

}
