

ok

  npm install --save graphql typescript apollo-server ts-node @types/request
Or I guess just npm install at this point


run via
  npx ts-node ./src/index.ts


This sorta adds two new things that have to be kept in sync, bringing the total up to four.
You could argue it actually adds 3, bringing the total to five copies of each description of these objects.

 - Java class in common folder: lacks an actual {virtue/vm/whatever} attribute, has non-common methods
 - .ts class in workbench: has non-common methods, but could use all the same attributes if you made a children() method
    that returned the correct attribute)
 - graphql schema definition of Input{User/Virtue/etc}: most of the attributes; same as java class.
 - graphql schema type: has all the attributes.
 - ts type in graphql server: same as Input* schema.

 So really only two definitions, but spread across three different languages.


 So.. Use java as a base, and add a couple attributes where necessary? I don't want to generate a portion of an annotated java class dynamically.

So.. Read in java file (ugh), parse out attributes and types.
Hardcode in which values are non-nullable in schema, to add the '!'.

That is not trivial. Not terrible, but not a day's work.

Well maybe we can put that off till next session, when we can think about the amount of data coming in. Or will we need to already be able to handle
all that data at the get-go, so we can do all the needed analytics?
Well this won't actually speed much up, only make some parts of the code much easier, abstract the rest calls to one place, and allow the browser to only have to pull down the values it needs.
  Oh what if something just looked through the columns to find what values to request? But there's more than tables, and there's no need to start bringing in that level of reflection, to find everything the page will need.
Anyway, this would only speed up the front end by a constant factor. Which may be helpful eventually, but probably isn't now. The biggest help is just that it would have made pulling all the items much easier. Is there anything we'll need to connect? Instances. Sensors. Settings. The Items we already have. It would be useful for instances, and probably sensors. It'd provide a mechanism for doing analytics on the server, and could essentially turn into a middle-end. That might be useful. Have endpoints in the server schema that make calls to local programs that can take in all the sensor data and return something more useful. That could be done client side too, I guess. But not in the browser. We could somehow hook into jupyter or something? Have the sysadmin run that locally?

Having a GraphQL server would be especially useful when implementing the sensor-displays and instance view-pages if the sysadmin could pick and choose what values they wanted to request. It'd at least be useful for the sensors. The instances you could just put the important stuff in a table and throw everything else in a new set of view pages. Arbitrary columns would be useful though.



example query:

{
  users {
    username
    enabled
    virtues {
      name
    }
  }
}

mutation{
  addUser( newUserData:{username: "Eve2"}) {
    username
    enabled
    virtues {
      name
    }
  }
}
