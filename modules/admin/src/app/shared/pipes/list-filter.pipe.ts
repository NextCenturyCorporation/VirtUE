import { Pipe, PipeTransform } from '@angular/core';
/***
This filter can be used with virtue, virtual machine list pages
 */
@Pipe({
  name: 'listFilterSort'
})
export class ListFilterPipe implements PipeTransform {

  /**
   * @param list the list to be filtered.
   * @param sortColumn the name of the attribute in each table element, which the list should be sorted on.
   * @param sortDirection the direction in which the list should be sorted. Should be either 'asc' or 'desc'
   * @param filterColumn the name of the attribute in each table element which should be used, when applying the filter.
   * @param filterCondition a function that returns true if the item, with its given attribute, should remain in the list.
   *    Defined in the component that defined the GenericTableComponent.
   *  - #TODO currently it defaults to sorting on the status column (item.enabled), but that should be
   *    set/defined in gen-list and not gen-table
   *  - Should look like: (attribute: any) => {return someBooleanConditional;}
   *
   * @param update completely ignored, but a chage makes the table being filtered re-render itself.
   *
   * @return
   */
  transform(list: any[],
            sortColumn: string, sortDirection: string,
            filterColumn: string, filterCondition: ((attribute) => boolean),
            update: boolean
  ): any[] {
    if (list.length < 2) {
      return list;
    }

    // this sorts list in-place, and so doesn't need to return anything.
    this.sortList(list, sortColumn, sortDirection);

    // filterList actually returns a new list, and so its return value must be saved.
    return this.filterList(list, filterColumn, filterCondition)
  }

  /**
   * Filters the input list.
   * Only lets values through, for which filterCondition returns true.
   *
   * @param list the list to be filtered.
   * @param filterColumn the name of the attribute in each elemen which should be used to filter it in or out.
   * @param filterCondition a function, defined in the component that defined the GenericTableComponent. See note
   *    on [[transform]].
   *
   * @return the list, filtered
   */
  filterList(list: any[], filterColumn: string, filterCondition: ((attribute) => boolean)): any[] {

    let filteredList = list.filter(element => filterCondition(element[filterColumn]));

    return filteredList;
  }

  /**
   * Sorts a input list in-place, based on given paramters.
   *
   * @param list the list to be filtered.
   * @param sortColumn the name of the attribute in each table element, which the list should be sorted on.
   * @param sortDirection the direction in which the list should be sorted. Should be either 'asc' or 'desc'
   */
  sortList(list: any[], propertyName: string, sortDirection: string): void {

    if (sortDirection === 'desc') {
      list.sort((leftSide, rightSide): number => {
        let left = leftSide[propertyName];
        let right = rightSide[propertyName];
        if (typeof left === 'string') {
          left = left.toUpperCase();
        }
        if (typeof right === 'string') {
          right = right.toUpperCase();
        }
        if (left < right) {
          return 1;
        }
        if (left > right) {
          return -1;
        }
        return 0;
      });
    } else {
      list.sort((leftSide, rightSide): number => {
        let left = leftSide[propertyName];
        let right = rightSide[propertyName];
        if (typeof left === 'string') {
          left = left.toUpperCase();
        }
        if (typeof right === 'string') {
          right = right.toUpperCase();
        }
        if (left < right) {
          return -1;
        }
        if (left > right) {
          return 1;
        }
        return 0;
      });
    }
  }
}
