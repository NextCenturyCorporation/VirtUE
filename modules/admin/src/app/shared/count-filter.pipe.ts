import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'count'
})
export class CountFilterPipe implements PipeTransform {

  transform(value: any, filterString: string, propName: string) {

    if (value.length === 0 || filterString === '') {
      return value.length;
    }
    const resultArray = [];
    let resultCount;


    for (const item of value) {
      if (item[propName].toLowerCase().match(filterString)) {
        resultArray.push(item);
      }
    }

    return resultArray.length;
  }

}
