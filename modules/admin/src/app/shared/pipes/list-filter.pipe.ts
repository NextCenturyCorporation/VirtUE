import { Pipe, PipeTransform } from '@angular/core';
/***
This filter can be used with virtue, virtual machine list pages
 */
@Pipe({
  name: 'listFilter'
})
export class ListFilterPipe implements PipeTransform {
  transform(value: any, filterColumn: string, filterType: string, filterValue: any, filterDirection: string) {
    if (value.length < 1) {
      return value;
    }

    let sortedList = value.slice(0);
    let resultArray = [];

    //filterValue can be true, false, or *. Default is *.
    if (filterType === 'enabled' && filterValue === true) {
      sortedList = value.filter(result => result[filterType] === filterValue);
    } else if (filterType === 'enabled' && filterValue === false) {
      sortedList = value.filter(result => result[filterType] === filterValue);
    } else {
      sortedList = value;
    }
    resultArray = this.sortByColumn(sortedList, filterColumn, filterValue, filterDirection);
    return resultArray;
  }

  sortByColumn(data: any, column: string, columnValue: any, filterDirection: string) {
    // if (column === 'enabled') {
    //   let sortList = data.filter(results => results[column] === columnValue);
    //   return this.sortData(sortList, column, filterDirection);
    // } else {
      let sortList = this.sortData(data, column, filterDirection);
      return sortList;
    // }
  }

  sortData (data: any, propertyName: string, filterDirection: string) {
    if (propertyName === 'date') {
      propertyName = 'lastModification';
    }
    if (filterDirection === 'desc') {
      // console.log('sorting list by desc order');
      data.sort((leftSide, rightSide): number => {
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
      data.sort((leftSide, rightSide): number => {
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
    return data;
  }
}
