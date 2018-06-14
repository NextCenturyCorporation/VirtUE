import { Pipe, PipeTransform } from '@angular/core';
/***
This filter can be used with virtue, virtual machine list pages
 */
@Pipe({
  name: 'listFilter'
})
export class ListFilterPipe implements PipeTransform {
  transform(value: any, filterType: string, filterValue: any, filterDirection: string) {
    if (value.length === 0) {
      return value;
    }
    console.log('listFilter info: ');
    console.log('filterType: ' + filterType);
    console.log('filterValue: ' + filterValue);
    console.log('filterDirection: ' + filterDirection);

    let propertyName = filterType;
    let resultArray = [];
    let sortedList = value.slice(0);

    if (filterType === 'enabled') {
      propertyName = 'name';
    }
    // sortedList = this.sortData(sortedList, filterType, filterDirection);
    resultArray = this.sortByColumn(sortedList, propertyName, filterValue, filterDirection);
    // if ((filterType === 'enabled' || filterType === 'name') && filterValue === '*') {
    //   resultArray = sortedList;
    // } else if (filterType === 'enabled' && filterValue !== '*') {
    //   resultArray = sortedList.filter(result => result[filterType] === filterValue);
    //   resultArray = this.sortData(resultArray, propertyName, filterDirection);
    //   console.log(resultArray);
    // } else {
    //   resultArray = sortedList;
    // }
    console.log(resultArray);
    return resultArray;
  }

  sortByColumn(data: any, column: string, columnValue: any, filterDirection: string) {
    if (column === 'enabled') {
      let sortList = data.filter(results => results[column] === columnValue);
      return this.sortData(sortList, 'name', filterDirection);
    } else {
      let sortList = this.sortData(data, column, filterDirection);
      return sortList;
    }
  }

  sortData (data: any, propertyName: string, filterDirection: string) {
    if (filterDirection === 'desc') {
      console.log('sorting list by desc order');
      data.sort((leftSide, rightSide): number => {
        if (leftSide[propertyName] < rightSide[propertyName]) {
          return 1;
        }
        if (leftSide[propertyName] > rightSide[propertyName]) {
          return -1;
        }
        return 0;
      });
    } else {
      console.log('sorting list by asc order');
      data.sort((leftSide, rightSide): number => {

        if (leftSide[propertyName] < rightSide[propertyName]) {
          return -1;
        }
        if (leftSide[propertyName] > rightSide[propertyName]) {
          return 1;
        }
        return 0;
      });
    }
    return data;
  }
}
