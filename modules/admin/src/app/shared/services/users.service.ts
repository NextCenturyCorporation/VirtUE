import { EventEmitter, Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs/observable';
import { User } from '../models/user.model';
import { Virtue } from '../models/virtue.model';

const httpOption = {
  headers: new HttpHeaders({ 'Content-Type':'application/json' })
}

@Injectable()

export class UsersService {

  // private jsondata = 'http://localhost:8080/admin/user/template';
  private adUsers = './assets/json/ad_users.json';
  private jsondata = './assets/json/app_users.json';

  // use this when running locally only
  private userData = [
    {
      "id": "30SK",
      "name": "Sophie Kim",
      "ad_id": [
        {
          "ad_id":"skim",
          "name":"Sophie Kim",
          "company":"Next Century Corp",
          "ad_class":"Staff"
        }
      ],
      "virtues": [
        {
          "id": "86793b5b-6dd2-40ad-be14-170d775a7b9f",
          "name": "Web Virtue",
          "version": "1.0",
          "vmTemplates": [{
            "id": "bd3d540b-d375-4b2f-a7b7-e14159fcb60b",
            "name": "Browsers",
            "os": "LINUX",
            "templatePath": "Browsers",
            "applications": [{
              "id": "db2fd10a-c46b-4795-a3d4-a0c5ef8de5ea",
              "name": "Chrome",
              "version": "1.0",
              "os": "LINUX",
              "launchCommand": "google-chrome"
            }, {
              "id": "fde40cef-5b32-44e7-8a2e-9266a9e3ac64",
              "name": "Firefox",
              "version": "1.0",
              "os": "LINUX",
              "launchCommand": "firefox"
            }],
            "enabled": true,
            "lastModification": null,
            "lastEditor": null
          }],
          "enabled": true,
          "lastModification": "1/1/2018 10:00:00 AM",
          "lastEditor": "kdrumm",
          "awsTemplateName": "",
          "applications": [{
            "id": "fde40cef-5b32-44e7-8a2e-9266a9e3ac64",
            "name": "Firefox",
            "version": "1.0",
            "os": "LINUX",
            "launchCommand": "firefox"
          }, {
            "id": "db2fd10a-c46b-4795-a3d4-a0c5ef8de5ea",
            "name": "Chrome",
            "version": "1.0",
            "os": "LINUX",
            "launchCommand": "google-chrome"
          }]
        },
        {
          "id": "a2907b6a-2b2e-4cec-a8a3-c09ad64eff68",
          "name": "Artist Virtue",
          "version": "1.0",
          "vmTemplates": [{
            "id": "be0ca662-5203-4ef4-876a-9999cb30308e",
            "name": "Drawing",
            "os": "LINUX",
            "templatePath": "Drawing",
            "applications": [{
              "id": "8ff080ed-5105-415e-b50f-b929e262b57f",
              "name": "GIMP",
              "version": "1.0",
              "os": "LINUX",
              "launchCommand": "gimp"
            }, {
              "id": "095defbf-bbaa-4d9c-a722-589c5fdd8f41",
              "name": "Pinta",
              "version": "1.0",
              "os": "LINUX",
              "launchCommand": "pinta"
            }],
            "enabled": true,
            "lastModification": "1/15/2018 10:00:00 AM",
            "lastEditor": "kdrumm"
          }],
          "enabled": true,
          "lastModification": "1/5/2018 10:00:00 AM",
          "lastEditor": "kdrumm",
          "awsTemplateName": "",
          "applications": [{
            "id": "095defbf-bbaa-4d9c-a722-589c5fdd8f41",
            "name": "Pinta",
            "version": "1.0",
            "os": "LINUX",
            "launchCommand": "pinta"
          }, {
            "id": "8ff080ed-5105-415e-b50f-b929e262b57f",
            "name": "GIMP",
            "version": "1.0",
            "os": "LINUX",
            "launchCommand": "gimp"
          }]
        }
      ],
      "modifiedDate": "2017-12-05T19:57:01.052901",
      "status": "enabled"
    },
    {
      "id": "31CL",
      "name": "Chris Long",
      "ad_id": [
        {
          "ad_id":"clong",
          "name":"Chris Long",
          "company":"Next Century Corp",
          "ad_class":"Staff"
        }
      ],
      "virtues": [
        {
          "id": "86793b5b-6dd2-40ad-be14-170d775a7b9f",
          "name": "Web Virtue",
          "version": "1.0",
          "vmTemplates": [{
            "id": "bd3d540b-d375-4b2f-a7b7-e14159fcb60b",
            "name": "Browsers",
            "os": "LINUX",
            "templatePath": "Browsers",
            "applications": [{
              "id": "db2fd10a-c46b-4795-a3d4-a0c5ef8de5ea",
              "name": "Chrome",
              "version": "1.0",
              "os": "LINUX",
              "launchCommand": "google-chrome"
            }, {
              "id": "fde40cef-5b32-44e7-8a2e-9266a9e3ac64",
              "name": "Firefox",
              "version": "1.0",
              "os": "LINUX",
              "launchCommand": "firefox"
            }],
            "enabled": true,
            "lastModification": null,
            "lastEditor": null
          }],
          "enabled": true,
          "lastModification": "1/1/2018 10:00:00 AM",
          "lastEditor": "kdrumm",
          "awsTemplateName": "",
          "applications": [{
            "id": "fde40cef-5b32-44e7-8a2e-9266a9e3ac64",
            "name": "Firefox",
            "version": "1.0",
            "os": "LINUX",
            "launchCommand": "firefox"
          }, {
            "id": "db2fd10a-c46b-4795-a3d4-a0c5ef8de5ea",
            "name": "Chrome",
            "version": "1.0",
            "os": "LINUX",
            "launchCommand": "google-chrome"
          }]
        },
        {
          "id": "2bd0a5b7-8817-4008-b8ab-c45a23dfa901",
          "name": "Test Virtue",
          "version": "1.0",
          "vmTemplates": [{
            "id": "fcddc476-a6aa-4ffa-bfb6-a310b04ed5ad",
            "name": "All",
            "os": "LINUX",
            "templatePath": "All",
            "applications": [{
              "id": "db2fd10a-c46b-4795-a3d4-a0c5ef8de5ea",
              "name": "Chrome",
              "version": "1.0",
              "os": "LINUX",
              "launchCommand": "google-chrome"
            }, {
              "id": "fde40cef-5b32-44e7-8a2e-9266a9e3ac64",
              "name": "Firefox",
              "version": "1.0",
              "os": "LINUX",
              "launchCommand": "firefox"
            }, {
              "id": "9026d8ff-1eda-47e7-a517-0dbe2ada6efa",
              "name": "LibreOffice Calc",
              "version": "1.0",
              "os": "LINUX",
              "launchCommand": "localc"
            }, {
              "id": "15c45fa4-ef17-48cf-847f-e788f82f147f",
              "name": "LibreOffice Draw",
              "version": "1.0",
              "os": "LINUX",
              "launchCommand": "lodraw"
            }, {
              "id": "f292c015-aa8c-4724-8015-bd7fedaa4e31",
              "name": "LibreOffice Impress",
              "version": "1.0",
              "os": "LINUX",
              "launchCommand": "loimpress"
            }, {
              "id": "0986e242-6a4d-4c2a-8ae4-46dfcca2b018",
              "name": "LibreOffice Writer",
              "version": "1.0",
              "os": "LINUX",
              "launchCommand": "lowriter"
            }, {
              "id": "5cfc3bd3-b414-4cb9-9cf5-26738be0a066",
              "name": "Calculator",
              "version": "1.0",
              "os": "LINUX",
              "launchCommand": "gnome-calculator"
            }, {
              "id": "8ff080ed-5105-415e-b50f-b929e262b57f",
              "name": "GIMP",
              "version": "1.0",
              "os": "LINUX",
              "launchCommand": "gimp"
            }, {
              "id": "095defbf-bbaa-4d9c-a722-589c5fdd8f41",
              "name": "Pinta",
              "version": "1.0",
              "os": "LINUX",
              "launchCommand": "pinta"
            }, {
              "id": "5cfc3bd3-b414-4cb9-9cf5-26738be0a066",
              "name": "Calculator",
              "version": "1.0",
              "os": "LINUX",
              "launchCommand": "gnome-calculator"
            }],
            "enabled": true,
            "lastModification": "1/5/2018 10:00:00 AM",
            "lastEditor": "kdrumm"
          }],
          "enabled": true,
          "lastModification": "1/1/2018 10:00:00 AM",
          "lastEditor": "kdrumm",
          "awsTemplateName": "",
          "applications": [{
            "id": "8ff080ed-5105-415e-b50f-b929e262b57f",
            "name": "GIMP",
            "version": "1.0",
            "os": "LINUX",
            "launchCommand": "gimp"
          }, {
            "id": "db2fd10a-c46b-4795-a3d4-a0c5ef8de5ea",
            "name": "Chrome",
            "version": "1.0",
            "os": "LINUX",
            "launchCommand": "google-chrome"
          }, {
            "id": "fde40cef-5b32-44e7-8a2e-9266a9e3ac64",
            "name": "Firefox",
            "version": "1.0",
            "os": "LINUX",
            "launchCommand": "firefox"
          }, {
            "id": "15c45fa4-ef17-48cf-847f-e788f82f147f",
            "name": "LibreOffice Draw",
            "version": "1.0",
            "os": "LINUX",
            "launchCommand": "lodraw"
          }, {
            "id": "0986e242-6a4d-4c2a-8ae4-46dfcca2b018",
            "name": "LibreOffice Writer",
            "version": "1.0",
            "os": "LINUX",
            "launchCommand": "lowriter"
          }, {
            "id": "5cfc3bd3-b414-4cb9-9cf5-26738be0a066",
            "name": "Calculator",
            "version": "1.0",
            "os": "LINUX",
            "launchCommand": "gnome-calculator"
          }, {
            "id": "095defbf-bbaa-4d9c-a722-589c5fdd8f41",
            "name": "Pinta",
            "version": "1.0",
            "os": "LINUX",
            "launchCommand": "pinta"
          }, {
            "id": "f292c015-aa8c-4724-8015-bd7fedaa4e31",
            "name": "LibreOffice Impress",
            "version": "1.0",
            "os": "LINUX",
            "launchCommand": "loimpress"
          }, {
            "id": "9026d8ff-1eda-47e7-a517-0dbe2ada6efa",
            "name": "LibreOffice Calc",
            "version": "1.0",
            "os": "LINUX",
            "launchCommand": "localc"
          }]
        }
      ],
      "modifiedDate": "2017-12-05T19:57:01.052901",
      "status": "enabled"
    }
  ];
  virtueListChanged = new EventEmitter<UserVirtue[]>();
  private userVirtues : UserVirtue[]=[];

  constructor(
    private http: HttpClient
  ) {  }

  public getAdUsers(): Observable<Array<User>> {
    return this.http.get<Array<User>>(this.adUsers);
  }

  public getUser(id: string): Observable<any> {
    const src = `${this.jsondata}/${id}`;
    return this.http.get<User>(src);
  }

  public getLocalObj(id: string): Observable<any> {
    // console.log(this.userData);
    const data = this.userData;
    for (var i in data) {
        if (data[i].id === id) {
          return data[i];
          // console.log(data[i].slice());
          break;
        }
    }
  }

  public addUser(user: User): Observable<User> {
    return this.http.post<User>(this.jsondata, user);
  }

  public addUserVirtues(userVirtue: UserVirtue[]): Observable<Virtue> {
    // return this.http.post<User>(this.jsondata, user);
    this.userVirtues.push[userVirtue];
    console.log(userVirtue);
  }

  public getSelectedVirtues(){
    return this.virtues.slice();
  }

  public deleteUser(user: User): Observable<User> {
    return this.http.delete<User>(`${this.jsondata}/${user.id}`);
  }

  public listUsers(): Observable<Array<User>> {
    return this.http.get<Array<User>>(this.jsondata);
  }

  public update(user: User): Observable<User> {
    return this.http.put<User>(`${this.jsondata}/${user.id}`,user);
  }

}
