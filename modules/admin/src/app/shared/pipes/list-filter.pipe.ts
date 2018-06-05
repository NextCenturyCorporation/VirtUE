import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'listFilter'
})
export class ListFilterPipe implements PipeTransform {

  transform(value: any, filterValue: any, propName: string) {

    if (value.length === 0 || filterValue === '*') {
      return value;
    }
    const resultArray = [];

    console.log(propName);

    for (const item of value) {
      if (filterValue !== '*' && item[propName] === filterValue) {
        resultArray.push(item);
      }
    }

    return resultArray;
  }

}
