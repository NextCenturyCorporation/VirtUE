import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'filter'
})
export class JsonFilterPipe implements PipeTransform {

  transform(value: any, filterString: string, propName: string) {

    if (value.length === 0 || filterString === '') {
      return value;
    }
    const resultArray = [];
    const iResult = '';
    for (const item of value) {
      if (item[propName].toLowerCase().match(filterString)) {
        resultArray.push(item);
      }
    }
    return resultArray;

}
