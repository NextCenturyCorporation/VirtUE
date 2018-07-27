import { Pipe, PipeTransform } from '@angular/core';
/***
This filter can be used with virtue, virtual machine list pages
 */
@Pipe({
  name: 'listFilter'
})
export class ListFilterPipe implements PipeTransform {
  transform(list, sortColumn: string, filterColumn: string, filterValue: any, sortDirection: string, dontFilter: boolean) {
    if (list.length < 2) {
      return list;
    }
    if (!dontFilter) {
      list = this.filterList(list, filterColumn, filterValue);
    }

    return this.sortList(list, sortColumn, sortDirection);
  }

  //items with value 'status' =
  filterList(list, filterColumn: string, filterValue: string) {
    let filteredList = list.filter(element => (element[filterColumn] === filterValue));

    return filteredList;
  }

  sortList(list, propertyName: string, sortDirection: string) {

    if (sortDirection === 'desc') {
      // console.log('sorting list by desc order');
      list.sort((leftSide, rightSide): number => {
        let left = leftSide[propertyName];
        let right = rightSide[propertyName];
        if (typeof left ==='string') {
          left = left.toUpperCase();
        }
        if (typeof right ==='string') {
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
      // console.log('sorting list by asc order');
      list.sort((leftSide, rightSide): number => {
        let left = leftSide[propertyName];
        let right = rightSide[propertyName];
        if (typeof left ==='string') {
          left = left.toUpperCase();
        }
        if (typeof right ==='string') {
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
    return list;
  }
}
